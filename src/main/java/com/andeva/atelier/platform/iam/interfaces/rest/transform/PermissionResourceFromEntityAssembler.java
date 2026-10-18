package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.PermissionResource;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.responses.RolePermissionResource;

import java.util.Locale;
import java.util.Objects;

/**
 * Assembler projecting domain {@link Permission} entities into REST permission DTOs.
 * Both projections expose the same {@code category}: the lower case prefix of the name before the first ':'.
 *
 * @author Joel Huamani Estefanero
 */
public final class PermissionResourceFromEntityAssembler {

    private PermissionResourceFromEntityAssembler() {
    }

    /**
     * Converts a {@link Permission} entity into a catalog {@link PermissionResource}.
     *
     * @param permission Domain permission entity
     * @return REST response projection
     */
    public static PermissionResource toResourceFromEntity(Permission permission) {
        Objects.requireNonNull(permission, "Permission entity cannot be null");
        return new PermissionResource(
                permission.id().value(),
                permission.name(),
                permission.description(),
                categoryOf(permission.name())
        );
    }

    /**
     * Converts a {@link Permission} entity into a {@link RolePermissionResource} nested in role responses.
     *
     * @param permission Domain permission entity
     * @return REST response projection
     */
    public static RolePermissionResource toRolePermissionResourceFromEntity(Permission permission) {
        Objects.requireNonNull(permission, "Permission entity cannot be null");
        return new RolePermissionResource(
                permission.id().value(),
                permission.name(),
                categoryOf(permission.name())
        );
    }

    /**
     * Returns the lower case prefix of a permission name before its first ':' (e.g. {@code operations}).
     */
    static String categoryOf(String name) {
        int separator = name.indexOf(':');
        String prefix = separator >= 0 ? name.substring(0, separator) : name;
        return prefix.toLowerCase(Locale.ROOT);
    }
}
