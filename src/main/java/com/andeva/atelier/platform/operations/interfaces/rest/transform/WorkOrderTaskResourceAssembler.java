package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.TaskProductResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskImageResource;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.WorkOrderTaskResource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class WorkOrderTaskResourceAssembler {
    private WorkOrderTaskResourceAssembler() {}

    public static WorkOrderTaskResource toResourceFromEntity(WorkOrderTask task, String serviceName, String mechanicName) {
        if (task == null) {
            return null;
        }

        List<TaskProductResource> productResources = new ArrayList<>();
        if (task.getConsumedProducts() != null) {
            for (var prod : task.getConsumedProducts()) {
                productResources.add(new TaskProductResource(
                        prod.getId() != null ? prod.getId().value() : null,
                        task.getId() != null ? task.getId().value() : null,
                        prod.getProductId(),
                        "Product " + prod.getProductId(),
                        prod.getQuantity() != null ? prod.getQuantity().value() : BigDecimal.ZERO,
                        prod.getUnitPrice() != null ? prod.getUnitPrice().amount() : BigDecimal.ZERO,
                        prod.getTotalAmount() != null ? prod.getTotalAmount().amount() : BigDecimal.ZERO,
                        prod.getUnitPrice() != null && prod.getUnitPrice().currency() != null
                                ? prod.getUnitPrice().currency().name()
                                : "PEN"
                ));
            }
        }

        List<WorkOrderTaskImageResource> imageResources = new ArrayList<>();
        if (task.getTaskImages() != null) {
            for (var img : task.getTaskImages()) {
                imageResources.add(new WorkOrderTaskImageResource(
                        img.getId(),
                        task.getId() != null ? task.getId().value() : null,
                        img.getImageUrl() != null ? img.getImageUrl().value() : null,
                        img.getDescription(),
                        img.getUploadedAt()
                ));
            }
        }

        return new WorkOrderTaskResource(
                task.getId() != null ? task.getId().value() : null,
                task.getWorkOrderId() != null ? task.getWorkOrderId().value() : null,
                task.getServiceId() != null ? task.getServiceId().value() : null,
                serviceName,
                task.getMechanicId().orElse(null),
                mechanicName,
                task.getStatus() != null ? task.getStatus().name() : null,
                task.getDescription(),
                task.getPrice() != null ? task.getPrice().amount() : BigDecimal.ZERO,
                task.getPrice() != null && task.getPrice().currency() != null
                        ? task.getPrice().currency().name()
                        : "PEN",
                task.getHoldReason().map(HoldReason::name).orElse(null),
                task.getMissingItemDescription().orElse(null),
                task.getTotalPausedSeconds(),
                task.getStartedAt().orElse(null),
                task.getCompletedAt().orElse(null),
                productResources,
                imageResources
        );
    }
}
