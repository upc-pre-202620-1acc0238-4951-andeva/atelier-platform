package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.TaskProposalPersistenceEntity;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;

public final class TaskProposalPersistenceAssembler {

    private TaskProposalPersistenceAssembler() {
    }

    public static TaskProposal toDomain(TaskProposalPersistenceEntity entity) {
        if (entity == null) return null;

        WorkOrderId orderId = entity.getWorkOrder() != null ? WorkOrderId.of(entity.getWorkOrder().getId()) : null;
        WorkOrderTaskId taskId = entity.getTaskId() != null ? WorkOrderTaskId.of(entity.getTaskId()) : null;
        ServiceId serviceId = entity.getServiceId() != null ? ServiceId.of(entity.getServiceId()) : null;

        return new TaskProposal(
                entity.getId(),
                orderId,
                taskId,
                serviceId,
                entity.getMechanicId(),
                entity.getDescription(),
                entity.getSeverity(),
                StorageUrl.of(entity.getImageUrl()),
                entity.getStatus(),
                entity.getCustomerNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static TaskProposalPersistenceEntity toEntity(TaskProposal domain, WorkOrderPersistenceEntity parent) {
        if (domain == null) return null;

        TaskProposalPersistenceEntity entity = new TaskProposalPersistenceEntity();
        entity.setId(domain.getId());
        entity.setWorkOrder(parent);
        entity.setTaskId(domain.getTaskId().map(WorkOrderTaskId::value).orElse(null));
        entity.setServiceId(domain.getServiceId().map(ServiceId::value).orElse(null));
        entity.setMechanicId(domain.getMechanicId());
        entity.setDescription(domain.getDescription());
        entity.setSeverity(domain.getSeverity());
        entity.setImageUrl(domain.getImageUrl().value());
        entity.setStatus(domain.getStatus());
        entity.setCustomerNotes(domain.getCustomerNotes().orElse(null));
        return entity;
    }
}
