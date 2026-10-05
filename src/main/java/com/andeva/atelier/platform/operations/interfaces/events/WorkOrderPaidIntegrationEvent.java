package com.andeva.atelier.platform.operations.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderPaidIntegrationEvent(
        UUID workOrderId,
        UUID tenantId,
        Instant occurredOn
) {
}
