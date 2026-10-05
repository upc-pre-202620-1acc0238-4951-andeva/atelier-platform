package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.UpdateTaskProductQuantityCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.UpdateTaskProductResource;

import java.util.UUID;

public final class UpdateTaskProductQuantityCommandFromResourceAssembler {
    private UpdateTaskProductQuantityCommandFromResourceAssembler() {}

    public static UpdateTaskProductQuantityCommand toCommandFromResource(UUID taskId, UUID productId, UpdateTaskProductResource resource) {
        return new UpdateTaskProductQuantityCommand(
                new WorkOrderTaskId(taskId),
                productId,
                resource.quantity()
        );
    }
}
