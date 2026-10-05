package com.andeva.atelier.platform.operations.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkOrderCompletedIntegrationEvent(
        UUID workOrderId,
        UUID tenantId,
        UUID vehicleId,
        BigDecimal totalAmount,
        String currency,
        Instant occurredOn
) {
}
