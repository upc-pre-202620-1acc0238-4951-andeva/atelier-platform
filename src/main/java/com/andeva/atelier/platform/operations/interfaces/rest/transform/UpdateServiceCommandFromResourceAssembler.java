package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.UpdateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.UpdateServiceResource;

import java.util.UUID;

public final class UpdateServiceCommandFromResourceAssembler {
    private UpdateServiceCommandFromResourceAssembler() {}

    public static UpdateServiceItemCommand toCommandFromResource(UUID serviceId, UpdateServiceResource resource) {
        return new UpdateServiceItemCommand(
                new ServiceId(serviceId),
                resource.name(),
                resource.basePrice(),
                resource.currency() != null ? resource.currency() : "PEN",
                resource.estimatedMinutes()
        );
    }
}
