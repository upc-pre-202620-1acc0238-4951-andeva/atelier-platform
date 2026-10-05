package com.andeva.atelier.platform.operations.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderBayAssignedIntegrationEvent(
        UUID workOrderId,
        UUID tenantId,
        UUID bayId,
        String bayName,
        Instant occurredOn
) {
}
