package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a security role is assigned to a staff membership.
 *
 * @author Joel Huamani Estefanero
 */
public record RoleAssignedToMembershipEvent(
        TenantMembershipId membershipId,
        RoleId roleId,
        Instant occurredOn
) implements Serializable {

    public RoleAssignedToMembershipEvent {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static RoleAssignedToMembershipEvent of(TenantMembershipId membershipId, RoleId roleId) {
        return new RoleAssignedToMembershipEvent(membershipId, roleId, Instant.now());
    }
}
