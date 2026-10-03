package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.InvitationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a staff onboarding invitation is successfully accepted and redeemed by a user.
 *
 * @author Joel Huamani Estefanero
 */
public record StaffInvitationAcceptedEvent(
        InvitationId invitationId,
        TenantId tenantId,
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public StaffInvitationAcceptedEvent {
        Objects.requireNonNull(invitationId, "Invitation identifier cannot be null");
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(userId, "Accepted user identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static StaffInvitationAcceptedEvent of(
            InvitationId invitationId,
            TenantId tenantId,
            UserId userId) {
        return new StaffInvitationAcceptedEvent(invitationId, tenantId, userId, Instant.now());
    }
}
