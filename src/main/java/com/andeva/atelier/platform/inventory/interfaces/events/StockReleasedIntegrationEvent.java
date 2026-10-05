package com.andeva.atelier.platform.inventory.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockReleasedIntegrationEvent(
        UUID itemId,
        BigDecimal releasedQuantity,
        Instant occurredOn
) {
}
