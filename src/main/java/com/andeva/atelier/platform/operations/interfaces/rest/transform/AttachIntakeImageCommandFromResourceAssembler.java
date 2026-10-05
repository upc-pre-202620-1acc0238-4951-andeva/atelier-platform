package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.AttachIntakeImageCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.AttachImageResource;

import java.util.UUID;

public final class AttachIntakeImageCommandFromResourceAssembler {
    private AttachIntakeImageCommandFromResourceAssembler() {}

    public static AttachIntakeImageCommand toCommandFromResource(UUID workOrderId, AttachImageResource resource) {
        return new AttachIntakeImageCommand(
                new WorkOrderId(workOrderId),
                resource.imageUrl(),
                resource.description()
        );
    }
}
