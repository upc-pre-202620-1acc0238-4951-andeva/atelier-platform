package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;

import java.util.Objects;
import java.util.Set;

/**
 * Domain command to update the set of atomic permissions assigned to a security role.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateRolePermissionsCommand(
        RoleId roleId,
        Set<PermissionId> permissionIds
) {
    public UpdateRolePermissionsCommand {
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
        Objects.requireNonNull(permissionIds, "Permission IDs cannot be null");
        permissionIds = Set.copyOf(permissionIds);
    }
}
