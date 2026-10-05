package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.TaskProposalResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderDetailResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderImageResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderSummaryResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskResource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class WorkOrderResourceAssembler {
    private WorkOrderResourceAssembler() {}

    public static WorkOrderResource toResourceFromEntity(WorkOrder entity) {
        if (entity == null) {
            return null;
        }
        return new WorkOrderResource(
                entity.getId().value(),
                entity.getTenantId().value(),
                entity.getInternalNumber() != null ? entity.getInternalNumber().sequence() : null,
                entity.getVehicleId().value(),
                entity.getCustomerId().value(),
                entity.getBranchId().value(),
                entity.getCurrentBayId().map(WorkBayId::value).orElse(null),
                entity.getMileageIn() != null ? entity.getMileageIn().value() : null,
                entity.getStatus().name(),
                entity.getTotalAmount() != null ? entity.getTotalAmount().amount() : BigDecimal.ZERO,
                entity.getTotalAmount() != null && entity.getTotalAmount().currency() != null
                        ? entity.getTotalAmount().currency().name()
                        : "PEN"
        );
    }

    public static WorkOrderSummaryResource toSummaryResourceFromEntity(WorkOrder entity, String bayName) {
        if (entity == null) {
            return null;
        }
        return new WorkOrderSummaryResource(
                entity.getId().value(),
                entity.getTenantId().value(),
                entity.getInternalNumber() != null ? entity.getInternalNumber().sequence() : null,
                entity.getVehicleId().value(),
                entity.getCustomerId().value(),
                entity.getBranchId().value(),
                entity.getCurrentBayId().map(WorkBayId::value).orElse(null),
                bayName,
                entity.getStatus().name(),
                entity.getTotalAmount() != null ? entity.getTotalAmount().amount() : BigDecimal.ZERO,
                entity.getTotalAmount() != null && entity.getTotalAmount().currency() != null
                        ? entity.getTotalAmount().currency().name()
                        : "PEN",
                Instant.now()
        );
    }

    public static WorkOrderDetailResource toDetailResourceFromEntity(
            WorkOrder entity,
            String bayName,
            List<WorkOrderTaskResource> taskResources,
            List<TaskProposalResource> proposalResources) {
        if (entity == null) {
            return null;
        }

        List<WorkOrderImageResource> imageResources = entity.getIntakeImages() != null
                ? entity.getIntakeImages().stream()
                .map(img -> new WorkOrderImageResource(
                        img.getId(),
                        entity.getId().value(),
                        img.getImageUrl() != null ? img.getImageUrl().value() : null,
                        img.getDescription(),
                        img.getUploadedAt()
                ))
                .collect(Collectors.toList())
                : Collections.emptyList();

        return new WorkOrderDetailResource(
                entity.getId().value(),
                entity.getTenantId().value(),
                entity.getInternalNumber() != null ? entity.getInternalNumber().sequence() : null,
                entity.getVehicleId().value(),
                entity.getCustomerId().value(),
                entity.getBranchId().value(),
                entity.getCurrentBayId().map(WorkBayId::value).orElse(null),
                bayName,
                entity.getMileageIn() != null ? entity.getMileageIn().value() : null,
                entity.getDiagnosticSummary() != null ? entity.getDiagnosticSummary().value() : null,
                entity.getStatus().name(),
                entity.getTotalAmount() != null ? entity.getTotalAmount().amount() : BigDecimal.ZERO,
                entity.getTotalAmount() != null && entity.getTotalAmount().currency() != null
                        ? entity.getTotalAmount().currency().name()
                        : "PEN",
                taskResources != null ? taskResources : Collections.emptyList(),
                proposalResources != null ? proposalResources : Collections.emptyList(),
                imageResources,
                Instant.now(),
                Instant.now()
        );
    }
}
