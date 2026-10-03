package com.andeva.atelier.platform.iam.domain.model.aggregates;

import com.andeva.atelier.platform.iam.domain.model.enums.InvitationStatus;
import com.andeva.atelier.platform.iam.domain.model.events.InvitationExpiredEvent;
import com.andeva.atelier.platform.iam.domain.model.events.InvitationRevokedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.StaffInvitationAcceptedEvent;
import com.andeva.atelier.platform.iam.domain.model.events.StaffInvitedEvent;
import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * Sovereign staff onboarding invitation aggregate root governing cryptographic token redemption.
 *
 * @author Joel Huamani Estefanero
 */
public class Invitation extends AbstractDomainAggregateRoot<Invitation> {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final InvitationId id;
    private final TenantId tenantId;
    private final EmailAddress email;
    private final String token;
    private InvitationStatus status;
    private final RoleId targetRoleId;
    private final Instant expiresAt;

    public Invitation(
            InvitationId id,
            TenantId tenantId,
            EmailAddress email,
            String token,
            InvitationStatus status,
            RoleId targetRoleId,
            Instant expiresAt) {
        this.id = Objects.requireNonNull(id, "Invitation identifier cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        this.email = Objects.requireNonNull(email, "Staff email address cannot be null");
        this.token = Objects.requireNonNull(token, "Security token cannot be null");
        this.status = Objects.requireNonNull(status, "Invitation status cannot be null");
        this.targetRoleId = Objects.requireNonNull(targetRoleId, "Target role identifier cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expiration instant cannot be null");
    }

    /**
     * Issues a new staff onboarding invitation with a cryptographic 32-byte URL-safe token.
     */
    public static Invitation issue(
            TenantId tenantId,
            EmailAddress email,
            RoleId targetRoleId,
            Duration validity) {
        Objects.requireNonNull(validity, "Validity duration cannot be null");

        InvitationId invitationId = InvitationId.generate();
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        Instant expiresAt = Instant.now().plus(validity);

        Invitation invitation = new Invitation(
                invitationId,
                tenantId,
                email,
                token,
                InvitationStatus.PENDING,
                targetRoleId,
                expiresAt
        );
        invitation.registerEvent(StaffInvitedEvent.of(invitationId, tenantId, email, token));
        return invitation;
    }

    /**
     * Redeems and accepts the invitation, transitioning status to ACCEPTED.
     *
     * @param acceptedByUserId user account that redeemed the invitation
     */
    public void accept(UserId acceptedByUserId) {
        Objects.requireNonNull(acceptedByUserId, "Redeeming user identifier cannot be null");
        if (!isPending()) {
            throw new IllegalStateException("Invitation is no longer pending or has expired");
        }
        this.status = InvitationStatus.ACCEPTED;
        registerEvent(StaffInvitationAcceptedEvent.of(this.id, this.tenantId, acceptedByUserId));
    }

    /**
     * Marks the invitation as expired.
     */
    public void expire() {
        this.status = InvitationStatus.EXPIRED;
        registerEvent(InvitationExpiredEvent.of(this.id));
    }

    /**
     * Revokes the invitation administratively before redemption.
     */
    public void revoke() {
        if (this.status == InvitationStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot revoke an invitation that has already been accepted");
        }
        this.status = InvitationStatus.REVOKED;
        registerEvent(InvitationRevokedEvent.of(this.id));
    }

    /**
     * Checks if the invitation is actively pending and not past its expiration date.
     */
    public boolean isPending() {
        return this.status == InvitationStatus.PENDING && this.expiresAt.isAfter(Instant.now());
    }

    public InvitationId id() {
        return id;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public EmailAddress email() {
        return email;
    }

    public String token() {
        return token;
    }

    public InvitationStatus status() {
        return status;
    }

    public RoleId targetRoleId() {
        return targetRoleId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invitation that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
