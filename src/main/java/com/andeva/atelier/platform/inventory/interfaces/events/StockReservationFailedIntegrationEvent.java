package com.andeva.atelier.platform.inventory.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockReservationFailedIntegrationEvent(
        UUID productId,
        UUID workOrderId,
        UUID taskId,
        BigDecimal requestedQuantity,
        String failureReason,
        Instant occurredOn
) {
}
