package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.AssignTaskMechanicCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.AssignTaskMechanicResource;

import java.util.UUID;

public final class AssignTaskMechanicCommandFromResourceAssembler {
    private AssignTaskMechanicCommandFromResourceAssembler() {}

    public static AssignTaskMechanicCommand toCommandFromResource(UUID taskId, AssignTaskMechanicResource resource) {
        return new AssignTaskMechanicCommand(
                new WorkOrderTaskId(taskId),
                resource.mechanicId()
        );
    }
}
