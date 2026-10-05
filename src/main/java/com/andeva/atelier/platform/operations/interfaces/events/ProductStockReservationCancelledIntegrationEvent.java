package com.andeva.atelier.platform.operations.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductStockReservationCancelledIntegrationEvent(
        UUID workOrderId,
        UUID taskId,
        UUID productId,
        BigDecimal quantity,
        Instant occurredOn
) {
}
