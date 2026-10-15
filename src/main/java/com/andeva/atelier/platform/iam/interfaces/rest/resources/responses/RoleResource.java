package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.List;
import java.util.UUID;

/**
 * Response projection representing an RBAC security role within a tenant.
 *
 * @param id           Universal unique identifier of the role
 * @param tenantId     Identifier of the workshop tenant owning the role
 * @param code         Factory template code (e.g. ROLE_MECHANIC); null for custom roles
 * @param name         Formal role name
 * @param description  Operational description of the role's scope
 * @param isSystemRole Flag indicating whether this is a platform factory template role
 * @param permissions  Permissions granted to the role, sorted by name
 * @author Joel Huamani Estefanero
 */
public record RoleResource(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        String description,
        boolean isSystemRole,
        List<RolePermissionResource> permissions
) {
    public RoleResource {
        permissions = permissions != null ? List.copyOf(permissions) : List.of();
    }
}
