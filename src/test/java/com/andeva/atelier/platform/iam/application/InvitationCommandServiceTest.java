package com.andeva.atelier.platform.iam.application;

import com.andeva.atelier.platform.iam.application.commandservices.InvitationCommandService;
import com.andeva.atelier.platform.iam.application.internal.commandservices.InvitationCommandServiceImpl;
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
import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.enums.UserStatus;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.repositories.InvitationRepository;
import com.andeva.atelier.platform.iam.domain.repositories.RoleRepository;
import com.andeva.atelier.platform.iam.domain.repositories.TenantMembershipRepository;
import com.andeva.atelier.platform.iam.domain.repositories.UserRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link InvitationCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Invitation Command Service Unit Tests")
class InvitationCommandServiceTest {

    @Mock
    private InvitationRepository invitationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private TenantMembershipRepository membershipRepository;
    @Mock
    private SubscriptionQuotaPort subscriptionQuotaPort;
    @Mock
    private BCryptHashingService hashingService;
    @Mock
    private BearerTokenService bearerTokenService;

    private InvitationCommandService invitationCommandService;

    private final TenantId tenantId = TenantId.generate();
    private final RoleId roleId = RoleId.generate();
    private final EmailAddress email = EmailAddress.of("newstaff@atelier.pe");

    @BeforeEach
    void setUp() {
        invitationCommandService = new InvitationCommandServiceImpl(
                invitationRepository,
                userRepository,
                roleRepository,
                membershipRepository,
                subscriptionQuotaPort,
                hashingService,
                bearerTokenService
        );
    }

    @Test
    @DisplayName("Should issue staff invitation successfully")
    void shouldIssueInvitationSuccessfully() {
        Role role = Role.createCustom(tenantId, "Mechanic", "Mechanic role", Set.of());
        InviteStaffCommand command = new InviteStaffCommand(tenantId, email, role.id(), Duration.ofDays(7));

        when(membershipRepository.countByTenantId(tenantId)).thenReturn(2L);
        doNothing().when(subscriptionQuotaPort).validateStaffAdditionAllowed(tenantId, 2);
        when(invitationRepository.findByTenantIdAndEmail(tenantId, email)).thenReturn(Optional.empty());
        when(roleRepository.findById(role.id())).thenReturn(Optional.of(role));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Result<Invitation, ApplicationError> result = invitationCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Invitation invitation = result.getOrThrow();
        assertThat(invitation.email()).isEqualTo(email);
        assertThat(invitation.status()).isEqualTo(InvitationStatus.PENDING);
        verify(invitationRepository).save(any(Invitation.class));
    }

    @Test
    @DisplayName("Should reject invitation when quota is exceeded")
    void shouldRejectWhenStaffQuotaExceeded() {
        InviteStaffCommand command = new InviteStaffCommand(tenantId, email, roleId, Duration.ofDays(7));

        when(membershipRepository.countByTenantId(tenantId)).thenReturn(5L);
        doThrow(new IllegalStateException("Staff quota exceeded for plan"))
                .when(subscriptionQuotaPort).validateStaffAdditionAllowed(tenantId, 5);

        Result<Invitation, ApplicationError> result = invitationCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("FORBIDDEN");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject invitation when pending invitation already exists")
    void shouldRejectWhenPendingInvitationExists() {
        InviteStaffCommand command = new InviteStaffCommand(tenantId, email, roleId, Duration.ofDays(7));
        Invitation existing = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));

        when(membershipRepository.countByTenantId(tenantId)).thenReturn(2L);
        doNothing().when(subscriptionQuotaPort).validateStaffAdditionAllowed(tenantId, 2);
        when(invitationRepository.findByTenantIdAndEmail(tenantId, email)).thenReturn(Optional.of(existing));

        Result<Invitation, ApplicationError> result = invitationCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("CONFLICT");
        verify(invitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept invitation, create user, membership, and return JWT session")
    void shouldAcceptInvitationSuccessfully() {
        Role role = Role.createCustom(tenantId, "Mechanic", "Workshop mechanic", Set.of(Permission.create("mro:exec", "Exec", "OPS")));
        Invitation invitation = Invitation.issue(tenantId, email, role.id(), Duration.ofDays(7));
        String token = invitation.token();

        AcceptInvitationCommand command = new AcceptInvitationCommand(
                token, "RawPassword123!", "Pedro", "Gómez", "+51999888777"
        );

        when(invitationRepository.findByToken(token)).thenReturn(Optional.of(invitation));
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(hashingService.hash("RawPassword123!")).thenReturn("$2a$12$abcdefghijklmnopqrstuvwxABCDEFGHIJKLMNOPQRSTUVWXYZ012");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findById(role.id())).thenReturn(Optional.of(role));
        when(bearerTokenService.generateToken(any(), eq(tenantId), any())).thenReturn("staff.jwt.token");

        Result<AuthenticatedUser, ApplicationError> result = invitationCommandService.handle(command);

        assertThat(result.isSuccess()).isTrue();
        AuthenticatedUser auth = result.getOrThrow();
        assertThat(auth.token()).isEqualTo("staff.jwt.token");
        assertThat(auth.user().status()).isEqualTo(UserStatus.ACTIVE);
        assertThat(invitation.status()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(auth.permissions()).contains("mro:exec");

        verify(invitationRepository).save(invitation);
        verify(membershipRepository).save(any(TenantMembership.class));
    }

    @Test
    @DisplayName("Should reject invitation acceptance when token is not found")
    void shouldRejectAcceptanceWhenTokenNotFound() {
        AcceptInvitationCommand command = new AcceptInvitationCommand(
                "invalid-token", "RawPassword123!", "Pedro", "Gómez", "+51999888777"
        );
        when(invitationRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        Result<AuthenticatedUser, ApplicationError> result = invitationCommandService.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().code()).isEqualTo("NOT_FOUND");
    }
}
