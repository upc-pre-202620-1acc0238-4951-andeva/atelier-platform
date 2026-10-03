package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.events.InvitationExpiredEvent;
import com.andeva.atelier.platform.iam.domain.model.events.InvitationRevokedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.StaffInvitationAcceptedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.StaffInvitedEvent;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit test suite for {@link Invitation} aggregate root.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Invitation Aggregate Root Unit Tests")
class InvitationAggregateTest {

    private final TenantId tenantId = TenantId.generate();
    private final EmailAddress email = EmailAddress.of("mecanico.nuevo@atelier.pe");
    private final RoleId roleId = RoleId.generate();

    @Test
    @DisplayName("Should issue invitation with cryptographic URL-safe token and register StaffInvitedEvent")
    void shouldIssueInvitation() {
        Invitation invitation = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));

        assertThat(invitation.id()).isNotNull();
        assertThat(invitation.tenantId()).isEqualTo(tenantId);
        assertThat(invitation.email()).isEqualTo(email);
        assertThat(invitation.targetRoleId()).isEqualTo(roleId);
        assertThat(invitation.status()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitation.token()).isNotBlank();
        assertThat(invitation.isPending()).isTrue();

        assertThat(invitation.domainEvents()).hasSize(1);
        assertThat(invitation.domainEvents().iterator().next())
                .isInstanceOf(StaffInvitedEvent.class);
    }

    @Test
    @DisplayName("Should accept pending invitation and register StaffInvitationAcceptedEvent")
    void shouldAcceptPendingInvitation() {
        Invitation invitation = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));
        invitation.clearDomainEvents();

        UserId acceptingUserId = UserId.generate();
        invitation.accept(acceptingUserId);

        assertThat(invitation.status()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation.isPending()).isFalse();

        assertThat(invitation.domainEvents()).hasSize(1);
        assertThat(invitation.domainEvents().iterator().next())
                .isInstanceOf(StaffInvitationAcceptedEvent.class);
    }

    @Test
    @DisplayName("Should reject accepting non-pending or expired invitation")
    void shouldRejectAcceptingNonPendingInvitation() {
        Invitation invitation = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));
        UserId user1 = UserId.generate();
        invitation.accept(user1);

        UserId user2 = UserId.generate();
        assertThatThrownBy(() -> invitation.accept(user2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no longer pending");
    }

    @Test
    @DisplayName("Should expire invitation and register InvitationExpiredEvent")
    void shouldExpireInvitation() {
        Invitation invitation = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));
        invitation.clearDomainEvents();

        invitation.expire();
        assertThat(invitation.status()).isEqualTo(InvitationStatus.EXPIRED);
        assertThat(invitation.isPending()).isFalse();

        assertThat(invitation.domainEvents()).hasSize(1);
        assertThat(invitation.domainEvents().iterator().next())
                .isInstanceOf(InvitationExpiredEvent.class);
    }

    @Test
    @DisplayName("Should revoke invitation and reject revoking already accepted invitation")
    void shouldRevokeInvitation() {
        Invitation invitation = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));
        invitation.clearDomainEvents();

        invitation.revoke();
        assertThat(invitation.status()).isEqualTo(InvitationStatus.REVOKED);
        assertThat(invitation.domainEvents()).hasSize(1);
        assertThat(invitation.domainEvents().iterator().next())
                .isInstanceOf(InvitationRevokedEvent.class);

        Invitation acceptedInvitation = Invitation.issue(tenantId, email, roleId, Duration.ofDays(7));
        acceptedInvitation.accept(UserId.generate());

        assertThatThrownBy(acceptedInvitation::revoke)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already been accepted");
    }
}
