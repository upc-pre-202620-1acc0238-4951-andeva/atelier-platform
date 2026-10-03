package com.andeva.atelier.platform.iam.domain.model.events;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a security role is revoked from a staff membership.
 *
 * @author Joel Huamani Estefanero
 */
public record RoleRevokedFromMembershipEvent(
        TenantMembershipId membershipId,
        RoleId roleId,
        Instant occurredOn
) implements Serializable {

    public RoleRevokedFromMembershipEvent {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
        Objects.requireNonNull(occurredOn, "Occurrence timestamp cannot be null");
    }

    public static RoleRevokedFromMembershipEvent of(TenantMembershipId membershipId, RoleId roleId) {
        return new RoleRevokedFromMembershipEvent(membershipId, roleId, Instant.now());
    }
}
