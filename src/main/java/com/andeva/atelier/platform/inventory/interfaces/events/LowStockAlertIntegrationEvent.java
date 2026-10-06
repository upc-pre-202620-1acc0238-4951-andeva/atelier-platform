package com.andeva.atelier.platform.inventory.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LowStockAlertIntegrationEvent(
        UUID itemId,
        UUID tenantId,
        BigDecimal currentStock,
        BigDecimal minimumStock,
        Instant occurredOn
) {
}
