package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.AddProductToTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.AddTaskProductResource;

import java.util.UUID;

public final class AddTaskProductCommandFromResourceAssembler {
    private AddTaskProductCommandFromResourceAssembler() {}

    public static AddProductToTaskCommand toCommandFromResource(UUID taskId, AddTaskProductResource resource) {
        return new AddProductToTaskCommand(
                new WorkOrderTaskId(taskId),
                resource.productId(),
                resource.quantity(),
                resource.unitPrice(),
                resource.currency() != null ? resource.currency() : "PEN"
        );
    }
}
