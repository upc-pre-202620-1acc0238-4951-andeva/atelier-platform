package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.ServicePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public final class ServicePersistenceAssembler {

    private ServicePersistenceAssembler() {
    }

    public static Service toDomain(ServicePersistenceEntity entity) {
        if (entity == null) return null;

        return new Service(
                ServiceId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                entity.getName(),
                Money.soles(entity.getBasePrice()),
                entity.getEstimatedTimeM()
        );
    }

    public static ServicePersistenceEntity toEntity(Service domain) {
        if (domain == null) return null;

        ServicePersistenceEntity entity = new ServicePersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setName(domain.getName());
        entity.setBasePrice(domain.getBasePrice().amount());
        entity.setEstimatedTimeM(domain.getEstimatedDurationMinutes());
        return entity;
    }

    public static void updateEntity(ServicePersistenceEntity entity, Service domain) {
        if (entity == null || domain == null) return;
        entity.setName(domain.getName());
        entity.setBasePrice(domain.getBasePrice().amount());
        entity.setEstimatedTimeM(domain.getEstimatedDurationMinutes());
    }
}
