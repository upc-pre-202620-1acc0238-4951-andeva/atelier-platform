package com.andeva.atelier.platform.operations.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderCreatedIntegrationEvent(
        UUID workOrderId,
        UUID tenantId,
        UUID vehicleId,
        Integer internalNumber,
        Instant occurredOn
) {
    public WorkOrderCreatedIntegrationEvent(UUID workOrderId, UUID tenantId, UUID customerId, UUID vehicleId, Integer internalNumber, Instant occurredOn) {
        this(workOrderId, tenantId, vehicleId, internalNumber, occurredOn);
    }
}
