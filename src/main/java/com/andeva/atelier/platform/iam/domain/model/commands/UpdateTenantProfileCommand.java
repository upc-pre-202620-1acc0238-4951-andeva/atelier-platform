package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain command to update a workshop Tenant's commercial and legal metadata.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateTenantProfileCommand(
        TenantId tenantId,
        String name,
        String legalName
) {
    public UpdateTenantProfileCommand {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Workshop commercial name cannot be null");
        Objects.requireNonNull(legalName, "Legal name cannot be null");
    }
}
