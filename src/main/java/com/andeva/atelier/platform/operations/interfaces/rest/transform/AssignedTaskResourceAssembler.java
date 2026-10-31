package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.application.queryservices.AssignedTaskView;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.AssignedTaskResource;

import java.util.List;

public final class AssignedTaskResourceAssembler {
    private AssignedTaskResourceAssembler() {}

    public static List<AssignedTaskResource> toResources(List<AssignedTaskView> views) {
        return views.stream().map(AssignedTaskResourceAssembler::toResource).toList();
    }

    public static AssignedTaskResource toResource(AssignedTaskView view) {
        var order = view.workOrder();
        var task = view.task();
        return new AssignedTaskResource(
                task.getId().value(),
                order.getId().value(),
                order.getInternalNumber().sequence(),
                order.getStatus().name(),
                order.getVehicleId().value(),
                order.getBranchId().value(),
                order.getCurrentBayId().map(WorkBayId::value).orElse(null),
                task.getStatus().name(),
                task.getDescription(),
                task.getEstimatedHours().value(),
                task.getStartedAt().orElse(null),
                task.getTotalPausedSeconds(),
                task.getPausedAt().orElse(null)
        );
    }
}
