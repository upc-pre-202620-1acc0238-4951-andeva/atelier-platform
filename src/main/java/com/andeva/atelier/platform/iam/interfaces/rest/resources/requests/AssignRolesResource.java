package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for assigning a collection of roles to a tenant membership.
 *
 * @param roleIds Non-empty list of unique identifiers of the roles to assign
 * @author Joel Huamani Estefanero
 */
public record AssignRolesResource(
        @NotEmpty
        List<UUID> roleIds
) {
}
