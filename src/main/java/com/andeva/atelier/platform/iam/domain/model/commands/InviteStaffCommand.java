package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Duration;
import java.util.Objects;

/**
 * Domain command to issue a staff onboarding invitation for a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record InviteStaffCommand(
        TenantId tenantId,
        EmailAddress email,
        RoleId targetRoleId,
        Duration validity
) {
    public InviteStaffCommand {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(email, "Staff email address cannot be null");
        Objects.requireNonNull(targetRoleId, "Target role identifier cannot be null");
        Objects.requireNonNull(validity, "Validity duration cannot be null");
    }
}
