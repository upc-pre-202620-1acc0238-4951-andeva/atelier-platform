package com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.Mileage;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.WorkOrderNumber;
import com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

public final class WorkOrderPersistenceAssembler {

    private WorkOrderPersistenceAssembler() {
    }

    public static WorkOrder toDomain(WorkOrderPersistenceEntity entity) {
        if (entity == null) return null;

        WorkBayId currentBayId = entity.getCurrentBayId() != null ? WorkBayId.of(entity.getCurrentBayId()) : null;

        WorkOrder order = new WorkOrder(
                WorkOrderId.of(entity.getId()),
                TenantId.of(entity.getTenantId()),
                BranchId.of(entity.getBranchId()),
                entity.getAppointmentId(),
                VehicleId.of(entity.getVehicleId()),
                CustomerId.of(entity.getCustomerId()),
                WorkOrderNumber.of(entity.getInternalNumber()),
                currentBayId,
                Mileage.of(entity.getMileageIn()),
                DiagnosticSummary.of(entity.getDiagnosticSummary()),
                Money.soles(entity.getSubtotal()),
                Money.soles(entity.getTax()),
                Money.soles(entity.getTotalAmount()),
                entity.getStatus()
        );

        if (entity.getTasks() != null) {
            entity.getTasks().forEach(t -> order.addExistingTask(WorkOrderTaskPersistenceAssembler.toDomain(t)));
        }

        if (entity.getProposals() != null) {
            entity.getProposals().forEach(p -> order.addExistingProposal(TaskProposalPersistenceAssembler.toDomain(p)));
        }

        if (entity.getImages() != null) {
            entity.getImages().forEach(img -> order.addExistingIntakeImage(WorkOrderImagePersistenceAssembler.toDomain(img)));
        }

        return order;
    }

    public static WorkOrderPersistenceEntity toEntity(WorkOrder domain) {
        if (domain == null) return null;

        WorkOrderPersistenceEntity entity = new WorkOrderPersistenceEntity();
        entity.setId(domain.getId().value());
        entity.setTenantId(domain.getTenantId().value());
        entity.setBranchId(domain.getBranchId().value());
        entity.setAppointmentId(domain.getAppointmentId().orElse(null));
        entity.setVehicleId(domain.getVehicleId().value());
        entity.setCustomerId(domain.getCustomerId().value());
        entity.setInternalNumber(domain.getInternalNumber().sequence());
        entity.setCurrentBayId(domain.getCurrentBayId().map(WorkBayId::value).orElse(null));
        entity.setMileageIn(domain.getMileageIn().value());
        entity.setDiagnosticSummary(domain.getDiagnosticSummary().value());
        entity.setSubtotal(domain.getSubtotal().amount());
        entity.setTax(domain.getTax().amount());
        entity.setTotalAmount(domain.getTotalAmount().amount());
        entity.setStatus(domain.getStatus());

        if (domain.getTasks() != null) {
            domain.getTasks().forEach(t -> entity.getTasks().add(WorkOrderTaskPersistenceAssembler.toEntity(t, entity)));
        }

        if (domain.getProposals() != null) {
            domain.getProposals().forEach(p -> entity.getProposals().add(TaskProposalPersistenceAssembler.toEntity(p, entity)));
        }

        if (domain.getIntakeImages() != null) {
            domain.getIntakeImages().forEach(img -> entity.getImages().add(WorkOrderImagePersistenceAssembler.toEntity(img, entity)));
        }

        return entity;
    }

    public static void updateEntity(WorkOrderPersistenceEntity entity, WorkOrder domain) {
        if (entity == null || domain == null) return;

        entity.setCurrentBayId(domain.getCurrentBayId().map(WorkBayId::value).orElse(null));
        entity.setMileageIn(domain.getMileageIn().value());
        entity.setDiagnosticSummary(domain.getDiagnosticSummary().value());
        entity.setSubtotal(domain.getSubtotal().amount());
        entity.setTax(domain.getTax().amount());
        entity.setTotalAmount(domain.getTotalAmount().amount());
        entity.setStatus(domain.getStatus());

        if (domain.getTasks() != null) {
            java.util.Map<java.util.UUID, com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity> existingTaskMap =
                    entity.getTasks().stream().collect(java.util.stream.Collectors.toMap(
                            com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity::getId,
                            t -> t,
                            (a, b) -> a
                    ));

            for (com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask domainTask : domain.getTasks()) {
                com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderTaskPersistenceEntity existingTask =
                        existingTaskMap.get(domainTask.getId().value());
                if (existingTask != null) {
                    WorkOrderTaskPersistenceAssembler.updateEntity(existingTask, domainTask);
                } else {
                    entity.getTasks().add(WorkOrderTaskPersistenceAssembler.toEntity(domainTask, entity));
                }
            }
        }

        if (domain.getProposals() != null) {
            java.util.Set<java.util.UUID> existingProposalIds = entity.getProposals().stream()
                    .map(com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.TaskProposalPersistenceEntity::getId)
                    .collect(java.util.stream.Collectors.toSet());

            for (com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal domainProp : domain.getProposals()) {
                if (!existingProposalIds.contains(domainProp.getId())) {
                    entity.getProposals().add(TaskProposalPersistenceAssembler.toEntity(domainProp, entity));
                } else {
                    entity.getProposals().stream()
                            .filter(p -> p.getId().equals(domainProp.getId()))
                            .findFirst()
                            .ifPresent(p -> {
                                p.setStatus(domainProp.getStatus());
                                p.setCustomerNotes(domainProp.getCustomerNotes().orElse(null));
                                p.setTaskId(domainProp.getTaskId().map(com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId::value).orElse(null));
                            });
                }
            }
        }

        if (domain.getIntakeImages() != null) {
            java.util.Set<java.util.UUID> existingImageIds = entity.getImages().stream()
                    .map(com.andeva.atelier.platform.operations.infrastructure.persistence.jpa.entities.WorkOrderImagePersistenceEntity::getId)
                    .collect(java.util.stream.Collectors.toSet());

            for (com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderImage domainImg : domain.getIntakeImages()) {
                if (!existingImageIds.contains(domainImg.getId())) {
                    entity.getImages().add(WorkOrderImagePersistenceAssembler.toEntity(domainImg, entity));
                }
            }
        }
    }
}
