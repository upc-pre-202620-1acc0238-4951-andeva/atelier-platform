package com.andeva.atelier.platform.inventory.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseOrderIssuedIntegrationEvent(
        UUID purchaseOrderId,
        UUID tenantId,
        UUID supplierId,
        String orderNumber,
        BigDecimal totalCost,
        Instant occurredOn
) {
}
