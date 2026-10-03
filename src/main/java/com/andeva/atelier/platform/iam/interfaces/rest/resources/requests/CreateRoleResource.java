package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for creating a custom RBAC role within a workshop tenant.
 *
 * @param name          Descriptive name of the custom role
 * @param description   Optional explanatory description of the role's responsibilities
 * @param permissionIds Non-empty list of atomic permission IDs to grant to this role
 * @author Joel Huamani Estefanero
 */
public record CreateRoleResource(
        @NotBlank @Size(max = 100)
        String name,

        @Size(max = 255)
        String description,

        @NotEmpty
        List<UUID> permissionIds
) {
}
