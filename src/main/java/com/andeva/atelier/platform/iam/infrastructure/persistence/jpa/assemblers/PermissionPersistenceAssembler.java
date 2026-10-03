package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.entities.Permission;
import com.andeva.atelier.platform.iam.domain.model.ids.PermissionId;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.PermissionPersistenceEntity;

/**
 * Bidirectional assembler converting between pure domain {@link Permission} entities
 * and relational {@link PermissionPersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class PermissionPersistenceAssembler {

    private PermissionPersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link Permission}.
     *
     * @param entity the persistence entity
     * @return initialized domain Permission, or null if entity is null
     */
    public static Permission toDomain(PermissionPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return Permission.of(
                PermissionId.of(entity.getId()),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory()
        );
    }

    /**
     * Converts a pure domain {@link Permission} into a JPA persistence entity.
     *
     * @param domain the domain entity
     * @return initialized persistence entity, or null if domain is null
     */
    public static PermissionPersistenceEntity toEntity(Permission domain) {
        if (domain == null) {
            return null;
        }
        return new PermissionPersistenceEntity(
                domain.id().value(),
                domain.name(),
                domain.description(),
                domain.category()
        );
    }
}
