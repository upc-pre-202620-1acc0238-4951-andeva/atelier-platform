package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTaskImage;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskImagePersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity;

public final class WorkOrderTaskImagePersistenceAssembler {

    private WorkOrderTaskImagePersistenceAssembler() {
    }

    public static WorkOrderTaskImage toDomain(WorkOrderTaskImagePersistenceEntity entity) {
        if (entity == null) return null;

        WorkOrderTaskId taskId = entity.getTask() != null ? WorkOrderTaskId.of(entity.getTask().getId()) : null;

        return new WorkOrderTaskImage(
                entity.getId(),
                taskId,
                StorageUrl.of(entity.getImageUrl()),
                entity.getEvidenceType(),
                entity.getDescription(),
                entity.getUploadedAt()
        );
    }

    public static WorkOrderTaskImagePersistenceEntity toEntity(WorkOrderTaskImage domain, WorkOrderTaskPersistenceEntity parent) {
        if (domain == null) return null;

        WorkOrderTaskImagePersistenceEntity entity = new WorkOrderTaskImagePersistenceEntity();
        entity.setId(domain.getId());
        entity.setTask(parent);
        entity.setImageUrl(domain.getImageUrl().value());
        entity.setEvidenceType(domain.getEvidenceType());
        entity.setDescription(domain.getDescription());
        entity.setUploadedAt(domain.getUploadedAt());
        return entity;
    }
}
