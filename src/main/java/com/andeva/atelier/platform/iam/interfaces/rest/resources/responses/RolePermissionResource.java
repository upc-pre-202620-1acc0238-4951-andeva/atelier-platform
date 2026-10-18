package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * Response projection of a permission granted to a role.
 *
 * @param id       Universal unique identifier of the permission, as accepted by {@code permissionIds}
 * @param name     Canonical permission code name (e.g., operations:tasks:track_time)
 * @param category Bounded context prefix of the name, in lower case (e.g., operations)
 * @author Joel Huamani Estefanero
 */
public record RolePermissionResource(
        UUID id,
        String name,
        String category
) {
}
