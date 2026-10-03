package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new staff employment relationship (TenantMembership) is established.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantMembershipCreatedEvent(
        TenantMembershipId membershipId,
        TenantId tenantId,
        UserId userId,
        Instant occurredOn
) implements Serializable {

    public TenantMembershipCreatedEvent {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(userId, "User identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static TenantMembershipCreatedEvent of(
            TenantMembershipId membershipId,
            TenantId tenantId,
            UserId userId) {
        return new TenantMembershipCreatedEvent(membershipId, tenantId, userId, Instant.now());
    }
}
