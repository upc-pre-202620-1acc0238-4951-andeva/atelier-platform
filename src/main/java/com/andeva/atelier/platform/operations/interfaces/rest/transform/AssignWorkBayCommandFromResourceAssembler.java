package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.AssignWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.AssignWorkBayResource;

import java.util.UUID;

public final class AssignWorkBayCommandFromResourceAssembler {
    private AssignWorkBayCommandFromResourceAssembler() {}

    public static AssignWorkBayCommand toCommandFromResource(UUID workOrderId, AssignWorkBayResource resource) {
        return new AssignWorkBayCommand(
                new WorkOrderId(workOrderId),
                new WorkBayId(resource.bayId())
        );
    }
}
