package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.AddTaskToWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CreateWorkOrderTaskResource;

import java.math.BigDecimal;
import java.util.UUID;

public final class CreateWorkOrderTaskCommandFromResourceAssembler {
    private CreateWorkOrderTaskCommandFromResourceAssembler() {}

    public static AddTaskToWorkOrderCommand toCommandFromResource(UUID workOrderId, CreateWorkOrderTaskResource resource) {
        return new AddTaskToWorkOrderCommand(
                new WorkOrderId(workOrderId),
                new ServiceId(resource.serviceId()),
                resource.mechanicId(),
                resource.description(),
                resource.price(),
                BigDecimal.ONE,
                resource.currency() != null ? resource.currency() : "PEN"
        );
    }
}
