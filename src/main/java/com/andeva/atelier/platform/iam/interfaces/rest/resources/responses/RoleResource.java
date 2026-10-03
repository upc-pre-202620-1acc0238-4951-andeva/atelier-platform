package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.List;
import java.util.UUID;

/**
 * Response projection representing an RBAC security role within a tenant.
 *
 * @param id           Universal unique identifier of the role
 * @param name         Formal role name
 * @param description  Operational description of the role's scope
 * @param isSystemRole Flag indicating whether this is a platform factory template role
 * @param permissions  Canonical permission code names granted to the role
 * @author Joel Huamani Estefanero
 */
public record RoleResource(
        UUID id,
        String name,
        String description,
        boolean isSystemRole,
        List<String> permissions
) {
    public RoleResource {
        permissions = permissions != null ? List.copyOf(permissions) : List.of();
    }
}
