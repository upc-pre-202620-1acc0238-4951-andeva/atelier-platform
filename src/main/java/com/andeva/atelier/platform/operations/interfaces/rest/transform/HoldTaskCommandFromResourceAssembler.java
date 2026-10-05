package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.HoldWorkOrderTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.HoldTaskResource;

import java.util.UUID;

public final class HoldTaskCommandFromResourceAssembler {
    private HoldTaskCommandFromResourceAssembler() {}

    public static HoldWorkOrderTaskCommand toCommandFromResource(UUID taskId, HoldTaskResource resource) {
        return new HoldWorkOrderTaskCommand(
                new WorkOrderTaskId(taskId),
                resource.missingItemDescription(),
                resource.inventoryItemId()
        );
    }
}
