package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.dto.GoogleUserPayload;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.GoogleIdentityGateway;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BearerTokenService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateWithGoogleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RegisterUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RefreshAccessTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.SignOutCommand;
import com.andeva.atelier.platform.iam.domain.model.aggregates.RefreshSession;
import com.andeva.atelier.platform.iam.domain.model.commands.ResendEmailVerificationCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.VerifyEmailTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.MembershipStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transactional orchestrator implementing User identity, authentication, and security token management.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final String EMAIL_NOT_VERIFIED = "EMAIL_NOT_VERIFIED";

    private final UserRepository userRepository;
    private final TenantMembershipRepository membershipRepository;
    private final BCryptHashingService hashingService;
    private final BearerTokenService bearerTokenService;
    private final GoogleIdentityGateway googleIdentityGateway;
    private final RefreshSessionManager refreshSessionManager;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            TenantMembershipRepository membershipRepository,
            BCryptHashingService hashingService,
            BearerTokenService bearerTokenService,
            GoogleIdentityGateway googleIdentityGateway
    ) {
        this(userRepository, membershipRepository, hashingService, bearerTokenService, googleIdentityGateway, null);
    }

    @Autowired
    public UserCommandServiceImpl(
            UserRepository userRepository,
            TenantMembershipRepository membershipRepository,
            BCryptHashingService hashingService,
            BearerTokenService bearerTokenService,
            GoogleIdentityGateway googleIdentityGateway,
            RefreshSessionManager refreshSessionManager
    ) {
        this.refreshSessionManager = refreshSessionManager;
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
        this.hashingService = Objects.requireNonNull(hashingService, "BCryptHashingService cannot be null");
        this.bearerTokenService = Objects.requireNonNull(bearerTokenService, "BearerTokenService cannot be null");
        this.googleIdentityGateway = Objects.requireNonNull(googleIdentityGateway, "GoogleIdentityGateway cannot be null");
    }

    @Override
    public Result<User, ApplicationError> handle(RegisterUserCommand command) {
        Objects.requireNonNull(command, "RegisterUserCommand cannot be null");

        if (userRepository.existsByEmail(command.email())) {
            return Result.failure(ApplicationError.conflict(
                    "A user account with the specified email address already exists: " + command.email().value()));
        }

        User user = User.registerWithLocalCredentials(
                command.email(),
                command.password(),
                command.name(),
                command.phone()
        );
        user.issueVerificationToken(TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(15));
        User savedUser = userRepository.save(user);

        return Result.success(savedUser);
    }

    @Override
    public Result<AuthenticatedUser, ApplicationError> handle(AuthenticateUserCommand command) {
        Objects.requireNonNull(command, "AuthenticateUserCommand cannot be null");

        Optional<User> optionalUser = userRepository.findByEmail(command.email());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized("Invalid email or password credentials"));
        }

        User user = optionalUser.get();
        if (user.password() == null || !hashingService.matches(command.rawPassword(), user.password().hashedValue())) {
            return Result.failure(ApplicationError.unauthorized("Invalid email or password credentials"));
        }

        // Dedicated code so clients can route the user to the verification code step
        if (user.status() == UserStatus.PENDING_VERIFICATION) {
            return Result.failure(new ApplicationError(EMAIL_NOT_VERIFIED,
                    "The account email is not verified yet: enter the code sent by email or request a new one", List.of()));
        }

        if (user.status() != UserStatus.ACTIVE) {
            return Result.failure(ApplicationError.forbidden(
                    "User account is not active. Current status: " + user.status()));
        }

        return Result.success(buildAuthenticatedUser(user));
    }

    @Override
    public Result<AuthenticatedUser, ApplicationError> handle(AuthenticateWithGoogleCommand command) {
        Objects.requireNonNull(command, "AuthenticateWithGoogleCommand cannot be null");

        Optional<GoogleUserPayload> optionalPayload = googleIdentityGateway.verifyIdToken(command.idToken());
        if (optionalPayload.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized("Invalid or expired Google ID token"));
        }

        GoogleUserPayload payload = optionalPayload.get();
        EmailAddress email = EmailAddress.of(payload.email());

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            String firstName = (payload.givenName() != null && !payload.givenName().isBlank())
                    ? payload.givenName() : "Google";
            String lastName = (payload.familyName() != null && !payload.familyName().isBlank())
                    ? payload.familyName() : "User";
            PersonName name = PersonName.of(firstName, lastName);
            User newUser = User.registerWithGoogle(email, payload.googleId(), name);
            return userRepository.save(newUser);
        });

        // Google already proved ownership of the address: a local account still pending verification is activated
        if (user.status() == UserStatus.PENDING_VERIFICATION && payload.emailVerified()) {
            user.verifyEmail();
            user = userRepository.save(user);
        }

        if (user.status() != UserStatus.ACTIVE) {
            return Result.failure(ApplicationError.forbidden(
                    "User account is not active. Current status: " + user.status()));
        }

        return Result.success(buildAuthenticatedUser(user));
    }

    @Override
    public Result<Void, ApplicationError> handle(VerifyEmailTokenCommand command) {
        Objects.requireNonNull(command, "VerifyEmailTokenCommand cannot be null");

        Optional<User> optionalUser = (command.email() != null)
                ? userRepository.findByEmail(command.email())
                : userRepository.findByVerificationToken(command.token());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User", command.email() != null ? command.email().value() : command.token()));
        }

        User user = optionalUser.get();
        boolean consumed = user.validateAndConsumeToken(command.token(), TokenType.EMAIL_VERIFICATION);
        if (!consumed) {
            return Result.failure(ApplicationError.badRequest(
                    "The provided email verification token is invalid, expired, or already used"));
        }

        user.verifyEmail();
        userRepository.save(user);

        return Result.success(null);
    }

    @Override
    public Result<Void, ApplicationError> handle(ResendEmailVerificationCommand command) {
        Objects.requireNonNull(command, "ResendEmailVerificationCommand cannot be null");

        // Same answer whether or not the account exists, so the endpoint cannot be used to probe emails
        userRepository.findByEmail(command.email())
                .filter(user -> user.status() == UserStatus.PENDING_VERIFICATION)
                .ifPresent(user -> {
                    user.issueVerificationToken(TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(15));
                    userRepository.save(user);
                });

        return Result.success(null);
    }

    @Override
    public Result<Void, ApplicationError> handle(RequestPasswordResetCommand command) {
        Objects.requireNonNull(command, "RequestPasswordResetCommand cannot be null");

        Optional<User> optionalUser = userRepository.findByEmail(command.email());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.issueVerificationToken(TokenType.PASSWORD_RESET, Duration.ofHours(2));
            userRepository.save(user);
        }

        return Result.success(null);
    }

    @Override
    public Result<Void, ApplicationError> handle(ResetPasswordCommand command) {
        Objects.requireNonNull(command, "ResetPasswordCommand cannot be null");

        Optional<User> optionalUser = userRepository.findByVerificationToken(command.token());
        if (optionalUser.isEmpty()) {
            return Result.failure(ApplicationError.notFound("VerificationToken", command.token()));
        }

        User user = optionalUser.get();
        boolean consumed = user.validateAndConsumeToken(command.token(), TokenType.PASSWORD_RESET);
        if (!consumed) {
            return Result.failure(ApplicationError.badRequest(
                    "The provided password reset token is invalid, expired, or already used"));
        }

        user.updatePassword(command.newPassword());
        userRepository.save(user);

        return Result.success(null);
    }

    @Override
    public Result<AuthenticatedUser, ApplicationError> handle(RefreshAccessTokenCommand command) {
        Objects.requireNonNull(command, "RefreshAccessTokenCommand cannot be null");
        if (refreshSessionManager == null) {
            return Result.failure(ApplicationError.unauthorized("Session renewal is not available"));
        }
        Optional<RefreshSession> session = refreshSessionManager.findUsable(command.refreshToken());
        if (session.isEmpty()) {
            return Result.failure(ApplicationError.unauthorized("The refresh token is invalid, expired or revoked: sign in again"));
        }
        Optional<User> user = userRepository.findById(session.get().userId());
        if (user.isEmpty() || user.get().status() != UserStatus.ACTIVE) {
            refreshSessionManager.revoke(session.get());
            return Result.failure(ApplicationError.unauthorized("The account is no longer active"));
        }
        // Rotation: the presented token is spent and a new one is issued with the fresh access token
        refreshSessionManager.revoke(session.get());
        return Result.success(buildAuthenticatedUser(user.get()));
    }

    @Override
    public Result<Void, ApplicationError> handle(SignOutCommand command) {
        Objects.requireNonNull(command, "SignOutCommand cannot be null");
        if (refreshSessionManager != null) {
            refreshSessionManager.findUsable(command.refreshToken()).ifPresent(refreshSessionManager::revoke);
        }
        return Result.success(null);
    }

    private AuthenticatedUser buildAuthenticatedUser(User user) {
        List<TenantMembership> memberships = membershipRepository.findByUserId(user.id());
        TenantMembership activeMembership = memberships.stream()
                .filter(m -> m.status() == MembershipStatus.ACTIVE)
                .findFirst()
                .orElse(null);

        TenantId tenantId = activeMembership != null ? activeMembership.tenantId() : null;
        Set<String> permissions = new HashSet<>();

        if (activeMembership != null && activeMembership.assignedRoles() != null) {
            for (com.andeva.atelier.platform.iam.domain.model.aggregates.Role role : activeMembership.assignedRoles()) {
                // Factory roles expose their template code (e.g. ROLE_MECHANIC) for hasRole checks;
                // custom roles are defined exclusively by their granted catalog permissions.
                if (role.code() != null) {
                    permissions.add(role.code());
                }
                if (role.permissions() != null) {
                    role.permissions().forEach(p -> permissions.add(p.name()));
                }
            }
        }

        String token = bearerTokenService.generateToken(user, tenantId, permissions);
        AuthenticatedUser session = new AuthenticatedUser(user, token, tenantId, permissions)
                .withMembershipId(activeMembership != null ? activeMembership.id().value() : null);
        return refreshSessionManager != null ? session.withRefreshToken(refreshSessionManager.issue(user.id())) : session;
    }

    @Override
    @Transactional
    public Result<User, ApplicationError> handle(com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserProfileCommand command) {
        Objects.requireNonNull(command, "Command cannot be null");

        Optional<User> userOptional = userRepository.findById(command.userId());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User account not found with ID: " + command.userId().value()));
        }

        User user = userOptional.get();
        user.updateProfile(command.name(), command.phone());

        User savedUser = userRepository.save(user);
        return Result.success(savedUser);
    }

    @Override
    @Transactional
    public Result<Void, ApplicationError> handle(com.andeva.atelier.platform.iam.domain.model.commands.RegisterDeviceTokenCommand command) {
        Objects.requireNonNull(command, "Command cannot be null");

        Optional<User> userOptional = userRepository.findById(command.userId());
        if (userOptional.isEmpty()) {
            return Result.failure(ApplicationError.notFound("User account not found with ID: " + command.userId().value()));
        }

        User user = userOptional.get();
        user.updateFcmToken(command.fcmToken());
        userRepository.save(user);
        return Result.success(null);
    }
}
