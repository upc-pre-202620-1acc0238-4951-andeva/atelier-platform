package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.CancelWorkOrderCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CancelWorkOrderResource;

import java.util.UUID;

public final class CancelWorkOrderCommandFromResourceAssembler {
    private CancelWorkOrderCommandFromResourceAssembler() {}

    public static CancelWorkOrderCommand toCommandFromResource(UUID workOrderId, CancelWorkOrderResource resource) {
        return new CancelWorkOrderCommand(
                new WorkOrderId(workOrderId),
                resource.reason()
        );
    }
}
