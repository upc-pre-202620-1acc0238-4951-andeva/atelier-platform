package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderImage;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderImagePersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;

public final class WorkOrderImagePersistenceAssembler {

    private WorkOrderImagePersistenceAssembler() {
    }

    public static WorkOrderImage toDomain(WorkOrderImagePersistenceEntity entity) {
        if (entity == null) return null;

        WorkOrderId orderId = entity.getWorkOrder() != null ? WorkOrderId.of(entity.getWorkOrder().getId()) : null;

        return new WorkOrderImage(
                entity.getId(),
                orderId,
                StorageUrl.of(entity.getImageUrl()),
                entity.getDescription(),
                entity.getUploadedAt()
        );
    }

    public static WorkOrderImagePersistenceEntity toEntity(WorkOrderImage domain, WorkOrderPersistenceEntity parent) {
        if (domain == null) return null;

        WorkOrderImagePersistenceEntity entity = new WorkOrderImagePersistenceEntity();
        entity.setId(domain.getId());
        entity.setWorkOrder(parent);
        entity.setImageUrl(domain.getImageUrl().value());
        entity.setDescription(domain.getDescription());
        entity.setUploadedAt(domain.getUploadedAt());
        return entity;
    }
}
