package com.andeva.atelier.platform.operations.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderDeliveredIntegrationEvent(
        UUID workOrderId,
        UUID tenantId,
        UUID vehicleId,
        Instant occurredOn
) {
}
