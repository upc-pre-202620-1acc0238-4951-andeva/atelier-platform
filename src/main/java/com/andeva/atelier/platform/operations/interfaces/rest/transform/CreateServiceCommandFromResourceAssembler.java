package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceItemCommand;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.CreateServiceResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.UUID;

public final class CreateServiceCommandFromResourceAssembler {
    private CreateServiceCommandFromResourceAssembler() {}

    public static CreateServiceItemCommand toCommandFromResource(UUID tenantId, CreateServiceResource resource) {
        return new CreateServiceItemCommand(
                tenantId != null ? new TenantId(tenantId) : null,
                resource.name(),
                resource.basePrice(),
                resource.currency() != null ? resource.currency() : "PEN",
                resource.estimatedMinutes()
        );
    }
}
