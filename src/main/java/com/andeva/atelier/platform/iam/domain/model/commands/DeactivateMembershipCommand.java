package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.TenantMembershipId;

import java.util.Objects;

/**
 * Domain command to deactivate a staff member's contract within a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record DeactivateMembershipCommand(
        TenantMembershipId membershipId
) {
    public DeactivateMembershipCommand {
        Objects.requireNonNull(membershipId, "Membership identifier cannot be null");
    }
}
