package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;

import java.util.Objects;
import java.util.Set;

/**
 * Domain command to assign a set of security roles to a staff membership.
 *
 * @author Joel Huamani Estefanero
 */
public record AssignRolesToMembershipCommand(
        TenantMembershipId membershipId,
        Set<RoleId> roleIds
) {
    public AssignRolesToMembershipCommand {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
        Objects.requireNonNull(roleIds, "Role IDs set cannot be null");
        roleIds = Set.copyOf(roleIds);
        if (roleIds.isEmpty()) {
            throw new IllegalArgumentException("At least one role ID must be assigned");
        }
    }
}
