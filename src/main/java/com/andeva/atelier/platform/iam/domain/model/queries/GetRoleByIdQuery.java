package com.andeva.atelier.platform.iam.domain.model.queries;

import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;

import java.util.Objects;

/**
 * Domain query to retrieve a security role by its unique identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetRoleByIdQuery(
        RoleId roleId
) {
    public GetRoleByIdQuery {
        Objects.requireNonNull(roleId, "Role identifier cannot be null");
    }
}
