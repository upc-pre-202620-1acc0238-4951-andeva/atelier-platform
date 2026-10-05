package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.ServiceResource;

import java.math.BigDecimal;

public final class ServiceResourceAssembler {
    private ServiceResourceAssembler() {}

    public static ServiceResource toResourceFromEntity(Service entity) {
        if (entity == null) {
            return null;
        }
        return new ServiceResource(
                entity.getId().value(),
                entity.getTenantId().value(),
                entity.getName(),
                entity.getBasePrice() != null ? entity.getBasePrice().amount() : BigDecimal.ZERO,
                entity.getBasePrice() != null && entity.getBasePrice().currency() != null
                        ? entity.getBasePrice().currency().name()
                        : "PEN",
                entity.getEstimatedDurationMinutes()
        );
    }
}
