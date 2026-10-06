package com.andeva.atelier.platform.inventory.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockAllocatedIntegrationEvent(
        UUID allocationId,
        UUID itemId,
        BigDecimal allocatedQuantity,
        BigDecimal totalCogs,
        Instant occurredOn
) {
}
