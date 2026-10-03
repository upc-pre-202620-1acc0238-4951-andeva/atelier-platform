package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a staff membership contract is activated.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantMembershipActivatedEvent(
        TenantMembershipId membershipId,
        Instant occurredOn
) implements Serializable {

    public TenantMembershipActivatedEvent {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static TenantMembershipActivatedEvent of(TenantMembershipId membershipId) {
        return new TenantMembershipActivatedEvent(membershipId, Instant.now());
    }
}
