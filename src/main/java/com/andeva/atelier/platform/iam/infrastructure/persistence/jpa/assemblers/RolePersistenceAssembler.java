package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.aggregates.Role;
import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.RoleId;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.HashSet;
import java.util.Set;

/**
 * Bidirectional assembler converting between pure domain {@link Role} aggregates
 * and relational {@link RolePersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class RolePersistenceAssembler {

    private RolePersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link Role} aggregate root.
     *
     * @param entity the persistence entity
     * @return initialized domain Role, or null if entity is null
     */
    public static Role toDomain(RolePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        Set<Permission> permissions = new HashSet<>();
        if (entity.getPermissions() != null) {
            for (PermissionPersistenceEntity permEntity : entity.getPermissions()) {
                permissions.add(PermissionPersistenceAssembler.toDomain(permEntity));
            }
        }

        TenantId tenantId = entity.getTenant() != null
                ? TenantId.of(entity.getTenant().getId())
                : null;

        return new Role(
                RoleId.of(entity.getId()),
                tenantId,
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystemRole(),
                permissions
        );
    }

    /**
     * Converts a pure domain {@link Role} into a JPA persistence entity.
     *
     * @param domain the domain aggregate root
     * @param tenant the parent tenant persistence entity
     * @param permissions resolved permission persistence entities
     * @return initialized persistence entity, or null if domain is null
     */
    public static RolePersistenceEntity toEntity(
            Role domain,
            TenantPersistenceEntity tenant,
            Set<PermissionPersistenceEntity> permissions) {
        if (domain == null) {
            return null;
        }

        return new RolePersistenceEntity(
                domain.id().value(),
                tenant,
                domain.code(),
                domain.name(),
                domain.description(),
                domain.isSystemRole(),
                permissions != null ? permissions : new HashSet<>()
        );
    }
}
