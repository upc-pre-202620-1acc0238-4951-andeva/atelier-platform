package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.commandservices.UserCommandService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.dto.GoogleUserPayload;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.GoogleIdentityGateway;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BearerTokenService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateWithGoogleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RegisterUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.VerifyEmailTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.enums.TokenType;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link UserCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("User Command Service Unit Tests")
class UserCommandServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private BCryptHashingService hashingService;
    @Mock
    private BearerTokenService bearerTokenService;
    @Mock
    private GoogleIdentityGateway googleIdentityGateway;

    private UserCommandService userCommandService;

    private final EmailAddress email = EmailAddress.of("user@atelier.pe");
    private final Password password = Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012");
    private final PersonName name = PersonName.of("Carlos", "García");
    private final PhoneNumber phone = PhoneNumber.of("+51987654321");

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandServiceImpl(
                userRepository,
                membershipRepository,
                hashingService,
                bearerTokenService,
                googleIdentityGateway
        );
    }

    @Test
    @DisplayName("Should register user in PENDING_VERIFICATION and issue OTP token")
    void shouldRegisterUserSuccessfully() {
        RegisterUserCommand command = new RegisterUserCommand(email, password, name, phone);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<User, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        User user = result.getOrThrow();
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.status()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.verificationTokens()).hasSize(1);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject user registration when email already exists")
    void shouldRejectWhenEmailAlreadyExists() {
        RegisterUserCommand command = new RegisterUserCommand(email, password, name, phone);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        Result<User, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should authenticate active user with valid credentials and return JWT session")
    void shouldAuthenticateUserSuccessfully() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(email, "SecretPassword123!");
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        user.activate();

        TenantId tenantId = TenantId.generate();
        Role role = Role.createCustom(tenantId, "Admin", "Admin Role", Set.of(Permission.create("mro:read", "Read", "OPS")));
        TenantMembership membership = TenantMembership.create(tenantId, user.id(), SalaryType.FIXED, Money.soles(1000.0), Set.of(role));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hashingService.matches("SecretPassword123!", password.hashedValue())).thenReturn(true);
        when(membershipRepository.findByUserId(user.id())).thenReturn(List.of(membership));
        when(bearerTokenService.generateToken(eq(user), eq(tenantId), any())).thenReturn("mock.jwt.token");

        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        AuthenticatedUser auth = result.getOrThrow();
        assertThat(auth.token()).isEqualTo("mock.jwt.token");
        assertThat(auth.tenantId()).isEqualTo(tenantId);
        assertThat(auth.permissions()).contains("mro:read");
    }

    @Test
    @DisplayName("Should reject authentication when password does not match")
    void shouldRejectWhenPasswordMismatch() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(email, "WrongPassword");
        User user = User.registerWithLocalCredentials(email, password, name, phone);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hashingService.matches("WrongPassword", password.hashedValue())).thenReturn(false);

        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("Should reject authentication when user account is not active")
    void shouldRejectWhenUserNotActive() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(email, "SecretPassword123!");
        User user = User.registerWithLocalCredentials(email, password, name, phone); // status = PENDING_VERIFICATION

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(hashingService.matches("SecretPassword123!", password.hashedValue())).thenReturn(true);

        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("Should authenticate federated user with Google ID token")
    void shouldAuthenticateWithGoogleSuccessfully() {
        AuthenticateWithGoogleCommand command = new AuthenticateWithGoogleCommand("valid-google-id-token");
        GoogleUserPayload payload = new GoogleUserPayload("g-123", "googleuser@atelier.pe", "Lucia", "Perez", "http://photo.jpg", true);

        when(googleIdentityGateway.verifyIdToken("valid-google-id-token")).thenReturn(Optional.of(payload));
        when(userRepository.findByEmail(EmailAddress.of("googleuser@atelier.pe"))).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.findByUserId(any())).thenReturn(List.of());
        when(bearerTokenService.generateToken(any(), any(), any())).thenReturn("google.jwt.token");

        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        AuthenticatedUser auth = result.getOrThrow();
        assertThat(auth.token()).isEqualTo("google.jwt.token");
        assertThat(auth.user().email().value()).isEqualTo("googleuser@atelier.pe");
    }

    @Test
    @DisplayName("Should reject Google authentication with invalid token")
    void shouldRejectInvalidGoogleToken() {
        AuthenticateWithGoogleCommand command = new AuthenticateWithGoogleCommand("invalid-google-token");
        when(googleIdentityGateway.verifyIdToken("invalid-google-token")).thenReturn(Optional.empty());

        Result<AuthenticatedUser, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("UNAUTHORIZED");
    }

    @Test
    @DisplayName("Should verify email with valid OTP token and transition to ACTIVE")
    void shouldVerifyEmailSuccessfully() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        var token = user.issueVerificationToken(TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(15));
        VerifyEmailTokenCommand command = new VerifyEmailTokenCommand(email, token.tokenValue());

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Void, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should reject email verification with incorrect token")
    void shouldRejectInvalidVerificationToken() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        user.issueVerificationToken(TokenType.EMAIL_VERIFICATION, Duration.ofMinutes(15));
        VerifyEmailTokenCommand command = new VerifyEmailTokenCommand(email, "999999");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Result<Void, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("BAD_REQUEST");
    }

    @Test
    @DisplayName("Should issue password reset token upon request")
    void shouldRequestPasswordResetSuccessfully() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        RequestPasswordResetCommand command = new RequestPasswordResetCommand(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Result<Void, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(user.verificationTokens()).hasSize(1);
        assertThat(user.verificationTokens().get(0).type()).isEqualTo(TokenType.PASSWORD_RESET);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should reset password with valid token")
    void shouldResetPasswordSuccessfully() {
        User user = User.registerWithLocalCredentials(email, password, name, phone);
        var token = user.issueVerificationToken(TokenType.PASSWORD_RESET, Duration.ofHours(2));
        Password newPassword = Password.of("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012");
        ResetPasswordCommand command = new ResetPasswordCommand(token.tokenValue(), newPassword);

        when(userRepository.findByVerificationToken(token.tokenValue())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Void, ApplicationError> result = userCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(user.password()).isEqualTo(newPassword);
        verify(userRepository).save(user);
    }
}
