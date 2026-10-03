package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;
import java.util.Set;

/**
 * Domain command to formulate a custom security role within a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record CreateCustomRoleCommand(
        TenantId tenantId,
        String name,
        String description,
        Set<PermissionId> permissionIds
) {
    public CreateCustomRoleCommand {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Role name cannot be null");
        Objects.requireNonNull(description, "Role description cannot be null");
        Objects.requireNonNull(permissionIds, "Permission IDs cannot be null");
        permissionIds = Set.copyOf(permissionIds);
    }
}
