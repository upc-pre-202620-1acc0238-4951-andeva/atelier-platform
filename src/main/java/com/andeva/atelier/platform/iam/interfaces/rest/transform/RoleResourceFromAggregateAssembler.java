package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RolePermissionResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RoleResource;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Assembler projecting domain {@link Role} aggregates into REST {@link RoleResource} DTOs.
 *
 * @author Joel Huamani Estefanero
 */
public final class RoleResourceFromAggregateAssembler {

    private RoleResourceFromAggregateAssembler() {
    }

    /**
     * Converts a {@link Role} aggregate root into a {@link RoleResource}.
     *
     * @param role Domain role aggregate
     * @return REST response projection
     */
    public static RoleResource toResourceFromAggregate(Role role) {
        Objects.requireNonNull(role, "Role aggregate cannot be null");
        List<RolePermissionResource> permissions = role.permissions().stream()
                .sorted(Comparator.comparing(Permission::name))
                .map(PermissionResourceFromEntityAssembler::toRolePermissionResourceFromEntity)
                .toList();

        return new RoleResource(
                role.id().value(),
                role.tenantId().value(),
                role.code(),
                role.name(),
                role.description(),
                role.isSystemRole(),
                permissions
        );
    }
}
