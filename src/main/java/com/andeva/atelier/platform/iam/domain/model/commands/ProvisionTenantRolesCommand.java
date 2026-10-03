package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain command to provision initial factory role templates for a newly onboarded workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record ProvisionTenantRolesCommand(
        TenantId tenantId
) {
    public ProvisionTenantRolesCommand {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
    }
}
