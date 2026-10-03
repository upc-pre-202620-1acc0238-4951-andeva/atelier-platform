package com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.iam.domain.model.entities.Branch;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.BranchPersistenceEntity;
import com.andeva.atelier.platform.iam.infrastructure.persistence.jpa.entities.TenantPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Bidirectional assembler converting between pure domain {@link Branch} entities
 * and relational {@link BranchPersistenceEntity} instances.
 *
 * @author Joel Huamani Estefanero
 */
public final class BranchPersistenceAssembler {

    private BranchPersistenceAssembler() {
    }

    /**
     * Converts a JPA persistence entity into a pure domain {@link Branch}.
     *
     * @param entity the persistence entity
     * @return initialized domain Branch, or null if entity is null
     */
    public static Branch toDomain(BranchPersistenceEntity entity) {
        if (entity == null) {
            return null;
        }
        TenantId tenantId = entity.getTenant() != null
                ? TenantId.of(entity.getTenant().getId())
                : null;
        GeoPoint location = (entity.getLatitude() != null && entity.getLongitude() != null)
                ? GeoPoint.of(entity.getLatitude(), entity.getLongitude())
                : null;

        return new Branch(
                BranchId.of(entity.getId()),
                tenantId,
                entity.getName(),
                entity.getSunatCode() != null ? entity.getSunatCode() : "0000",
                location,
                entity.getGeofenceRadiusMeters(),
                entity.isActive()
        );
    }

    /**
     * Converts a pure domain {@link Branch} into a JPA persistence entity.
     *
     * @param domain the domain Branch
     * @param tenant the parent tenant persistence entity
     * @return initialized persistence entity, or null if domain is null
     */
    public static BranchPersistenceEntity toEntity(Branch domain, TenantPersistenceEntity tenant) {
        if (domain == null) {
            return null;
        }
        return new BranchPersistenceEntity(
                domain.id().value(),
                tenant,
                domain.name(),
                domain.sunatCode(),
                domain.location() != null ? domain.location().latitude() : null,
                domain.location() != null ? domain.location().longitude() : null,
                domain.geofenceRadiusMeters(),
                domain.isActive()
        );
    }
}
