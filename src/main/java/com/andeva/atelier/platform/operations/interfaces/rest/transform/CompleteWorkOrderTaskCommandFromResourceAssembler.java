package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.CompleteWorkOrderTaskCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CompleteTaskResource;

import java.util.UUID;

public final class CompleteWorkOrderTaskCommandFromResourceAssembler {
    private CompleteWorkOrderTaskCommandFromResourceAssembler() {}

    public static CompleteWorkOrderTaskCommand toCommandFromResource(UUID taskId, CompleteTaskResource resource) {
        return new CompleteWorkOrderTaskCommand(
                new WorkOrderTaskId(taskId),
                resource.actualLaborHours(),
                resource.notes()
        );
    }
}
