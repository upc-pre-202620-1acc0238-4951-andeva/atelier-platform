package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkBayPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

public final class WorkBayPersistenceAssembler {

    private WorkBayPersistenceAssembler() {
    }

    public static WorkBay toDomain(WorkBayPersistenceEntity entity) {
        if (entity == null) return null;

        WorkOrderId currentOrderId = entity.getCurrentWorkOrderId() != null ? WorkOrderId.of(entity.getCurrentWorkOrderId()) : null;

        return new WorkBay(
                WorkBayId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                entity.getName(),
                entity.getType(),
                entity.getStatus(),
                currentOrderId
        );
    }

    public static WorkBayPersistenceEntity toEntity(WorkBay domain) {
        if (domain == null) return null;

        WorkBayPersistenceEntity entity = new WorkBayPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setBranchId(domain.getBranchId().value());
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setStatus(domain.getStatus());
        domain.getCurrentWorkOrderId().ifPresent(id -> entity.setCurrentWorkOrderId(id.value()));
        return entity;
    }

    public static void updateEntity(WorkBayPersistenceEntity entity, WorkBay domain) {
        if (entity == null || domain == null) return;
        entity.setName(domain.getName());
        entity.setType(domain.getType());
        entity.setStatus(domain.getStatus());
        domain.getCurrentWorkOrderId().ifPresentOrElse(
                id -> entity.setCurrentWorkOrderId(id.value()),
                () -> entity.setCurrentWorkOrderId(null)
        );
    }
}
