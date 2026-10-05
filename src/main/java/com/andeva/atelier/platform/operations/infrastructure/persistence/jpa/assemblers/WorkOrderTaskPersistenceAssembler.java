package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

public final class WorkOrderTaskPersistenceAssembler {

    private WorkOrderTaskPersistenceAssembler() {
    }

    public static WorkOrderTask toDomain(WorkOrderTaskPersistenceEntity entity) {
        if (entity == null) return null;

        WorkOrderId orderId = entity.getWorkOrder() != null ? WorkOrderId.of(entity.getWorkOrder().getId()) : null;
        LaborHours estimatedHours = entity.getEstimatedHours() != null ? LaborHours.of(entity.getEstimatedHours()) : null;
        LaborHours actualHours = entity.getActualHours() != null ? LaborHours.of(entity.getActualHours()) : null;

        WorkOrderTask task = new WorkOrderTask(
                WorkOrderTaskId.of(entity.getId()),
                orderId,
                ServiceId.of(entity.getServiceId()),
                entity.getMechanicId(),
                entity.getStatus(),
                entity.getDescription(),
                Money.soles(entity.getPrice()),
                estimatedHours,
                actualHours,
                entity.getHoldReason(),
                entity.getMissingItemDescription(),
                entity.getPausedAt(),
                entity.getTotalPausedSeconds() != null ? entity.getTotalPausedSeconds() : 0L,
                entity.getStartedAt(),
                entity.getCompletedAt()
        );

        if (entity.getProducts() != null) {
            entity.getProducts().forEach(p -> task.addProduct(WorkOrderTaskProductPersistenceAssembler.toDomain(p)));
        }

        if (entity.getImages() != null) {
            entity.getImages().forEach(img -> task.addExistingImage(WorkOrderTaskImagePersistenceAssembler.toDomain(img)));
        }

        return task;
    }

    public static WorkOrderTaskPersistenceEntity toEntity(WorkOrderTask domain, WorkOrderPersistenceEntity parent) {
        if (domain == null) return null;

        WorkOrderTaskPersistenceEntity entity = new WorkOrderTaskPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setWorkOrder(parent);
        entity.setServiceId(domain.getServiceId().value());
        entity.setMechanicId(domain.getMechanicId().orElse(null));
        entity.setStatus(domain.getStatus());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice().amount());
        entity.setEstimatedHours(domain.getEstimatedHours().value());
        entity.setActualHours(domain.getActualHours().map(LaborHours::value).orElse(null));
        entity.setHoldReason(domain.getHoldReason().orElse(null));
        entity.setMissingItemDescription(domain.getMissingItemDescription().orElse(null));
        entity.setPausedAt(domain.getPausedAt().orElse(null));
        entity.setTotalPausedSeconds(domain.getTotalPausedSeconds());
        entity.setStartedAt(domain.getStartedAt().orElse(null));
        entity.setCompletedAt(domain.getCompletedAt().orElse(null));

        if (domain.getConsumedProducts() != null) {
            domain.getConsumedProducts().forEach(p ->
                    entity.getProducts().add(WorkOrderTaskProductPersistenceAssembler.toEntity(p, entity)));
        }

        if (domain.getTaskImages() != null) {
            domain.getTaskImages().forEach(img ->
                    entity.getImages().add(WorkOrderTaskImagePersistenceAssembler.toEntity(img, entity)));
        }

        return entity;
    }

    public static void updateEntity(WorkOrderTaskPersistenceEntity entity, WorkOrderTask domain) {
        if (entity == null || domain == null) return;
        entity.setMechanicId(domain.getMechanicId().orElse(null));
        entity.setStatus(domain.getStatus());
        entity.setDescription(domain.getDescription());
        entity.setPrice(domain.getPrice().amount());
        entity.setActualHours(domain.getActualHours().map(LaborHours::value).orElse(null));
        entity.setHoldReason(domain.getHoldReason().orElse(null));
        entity.setMissingItemDescription(domain.getMissingItemDescription().orElse(null));
        entity.setPausedAt(domain.getPausedAt().orElse(null));
        entity.setTotalPausedSeconds(domain.getTotalPausedSeconds());
        entity.setStartedAt(domain.getStartedAt().orElse(null));
        entity.setCompletedAt(domain.getCompletedAt().orElse(null));

        if (domain.getConsumedProducts() != null) {
            java.util.Set<java.util.UUID> existingProdIds = entity.getProducts().stream()
                    .map(com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskProductPersistenceEntity::getId)
                    .collect(java.util.stream.Collectors.toSet());
            for (var p : domain.getConsumedProducts()) {
                if (!existingProdIds.contains(p.getId().value())) {
                    entity.getProducts().add(WorkOrderTaskProductPersistenceAssembler.toEntity(p, entity));
                }
            }
        }

        if (domain.getTaskImages() != null) {
            java.util.Set<java.util.UUID> existingImgIds = entity.getImages().stream()
                    .map(com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskImagePersistenceEntity::getId)
                    .collect(java.util.stream.Collectors.toSet());
            for (var img : domain.getTaskImages()) {
                if (!existingImgIds.contains(img.getId())) {
                    entity.getImages().add(WorkOrderTaskImagePersistenceAssembler.toEntity(img, entity));
                }
            }
        }
    }
}
