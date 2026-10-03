package com.andeva.atelier.platform.iam.application.internal.commandservices;

import com.andeva.atelier.platform.iam.application.commandservices.InvitationCommandService;
import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.application.internal.outbound.acl.SubscriptionQuotaPort;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BCryptHashingService;
import com.andeva.atelier.platform.iam.application.internal.outbound.security.BearerTokenService;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AcceptInvitationCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.InviteStaffCommand;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.enums.SalaryType;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;
import com.andeva.atelier.platform.iam.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.iam.domain.repositories.InvitationRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Transactional orchestrator implementing staff onboarding invitation issuance and redemption.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional
public class InvitationCommandServiceImpl implements InvitationCommandService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantMembershipRepository membershipRepository;
    private final SubscriptionQuotaPort subscriptionQuotaPort;
    private final BCryptHashingService hashingService;
    private final BearerTokenService bearerTokenService;

    public InvitationCommandServiceImpl(
            InvitationRepository invitationRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            TenantMembershipRepository membershipRepository,
            SubscriptionQuotaPort subscriptionQuotaPort,
            BCryptHashingService hashingService,
            BearerTokenService bearerTokenService
    ) {
        this.invitationRepository = Objects.requireNonNull(invitationRepository, "InvitationRepository cannot be null");
        this.userRepository = Objects.requireNonNull(userRepository, "UserRepository cannot be null");
        this.roleRepository = Objects.requireNonNull(roleRepository, "RoleRepository cannot be null");
        this.membershipRepository = Objects.requireNonNull(membershipRepository, "TenantMembershipRepository cannot be null");
        this.subscriptionQuotaPort = Objects.requireNonNull(subscriptionQuotaPort, "SubscriptionQuotaPort cannot be null");
        this.hashingService = Objects.requireNonNull(hashingService, "BCryptHashingService cannot be null");
        this.bearerTokenService = Objects.requireNonNull(bearerTokenService, "BearerTokenService cannot be null");
    }

    @Override
    public Result<Invitation, ApplicationError> handle(InviteStaffCommand command) {
        Objects.requireNonNull(command, "InviteStaffCommand cannot be null");

        // 1. Validate subscription staff quota
        int currentStaffCount = (int) membershipRepository.countByTenantId(command.tenantId());
        try {
            subscriptionQuotaPort.validateStaffAdditionAllowed(command.tenantId(), currentStaffCount);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.forbidden(
                    "Staff invitation rejected due to subscription quota limits: " + ex.getMessage()));
        }

        // 2. Prevent duplicate pending invitations for the same email within this tenant
        Optional<Invitation> pending = invitationRepository.findByTenantIdAndEmail(command.tenantId(), command.email())
                .filter(Invitation::isPending);
        if (pending.isPresent()) {
            return Result.failure(ApplicationError.conflict(
                    "A pending invitation already exists for email address: " + command.email().value()));
        }

        // 3. Resolve target role and verify tenant ownership
        Optional<Role> optionalRole = roleRepository.findById(command.targetRoleId());
        if (optionalRole.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Role", command.targetRoleId().value().toString()));
        }

        Role role = optionalRole.get();
        if (!role.tenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.conflict(
                    "Target security role '" + role.name() + "' belongs to a different workshop tenant"));
        }

        // 4. Issue invitation with cryptographic token and validity
        Invitation invitation = Invitation.issue(command.tenantId(), command.email(), command.targetRoleId(), command.validity());
        Invitation savedInvitation = invitationRepository.save(invitation);

        return Result.success(savedInvitation);
    }

    @Override
    public Result<AuthenticatedUser, ApplicationError> handle(AcceptInvitationCommand command) {
        Objects.requireNonNull(command, "AcceptInvitationCommand cannot be null");

        // 1. Locate invitation by token
        Optional<Invitation> optionalInvitation = invitationRepository.findByToken(command.token());
        if (optionalInvitation.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Invitation", command.token()));
        }

        Invitation invitation = optionalInvitation.get();
        if (!invitation.isPending()) {
            return Result.failure(ApplicationError.badRequest(
                    "Staff invitation is no longer pending or has expired"));
        }

        // 2. Verify email uniqueness for new account
        if (userRepository.existsByEmail(invitation.email())) {
            return Result.failure(ApplicationError.conflict(
                    "A user account with email address " + invitation.email().value() + " already exists"));
        }

        // 3. Register user in ACTIVE state
        String passwordHash = hashingService.hash(command.rawPassword());
        Password password = Password.of(passwordHash);
        PersonName name = PersonName.of(command.firstName(), command.lastName());
        PhoneNumber phone = (command.phone() != null && !command.phone().isBlank())
                ? PhoneNumber.of(command.phone())
                : null;

        User user = User.registerWithLocalCredentials(invitation.email(), password, name, phone);
        user.activate();
        User savedUser = userRepository.save(user);

        // 4. Mark invitation as accepted
        invitation.accept(savedUser.id());
        invitationRepository.save(invitation);

        // 5. Create membership in tenant with invited role
        Role role = roleRepository.findById(invitation.targetRoleId()).orElseThrow();
        TenantMembership membership = TenantMembership.create(
                invitation.tenantId(),
                savedUser.id(),
                SalaryType.FIXED,
                Money.soles(0.00),
                Set.of(role)
        );
        membershipRepository.save(membership);

        // 6. Generate authenticated session
        Set<String> permissions = role.permissions().stream()
                .map(Permission::name)
                .collect(Collectors.toSet());
        String token = bearerTokenService.generateToken(savedUser, invitation.tenantId(), permissions);

        return Result.success(new AuthenticatedUser(savedUser, token, invitation.tenantId(), permissions));
    }
}
