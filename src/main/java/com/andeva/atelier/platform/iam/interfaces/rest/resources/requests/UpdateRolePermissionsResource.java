package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for sovereignly updating the permission set of a tenant role.
 *
 * @param permissionIds Non-empty list of permission IDs to assign to the role
 * @author Joel Huamani Estefanero
 */
public record UpdateRolePermissionsResource(
        @NotEmpty(message = "{iam.validation.role.permissions.required}")
        List<UUID> permissionIds
) {
}
