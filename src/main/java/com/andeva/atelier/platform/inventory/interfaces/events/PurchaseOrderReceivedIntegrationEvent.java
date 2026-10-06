package com.andeva.atelier.platform.inventory.interfaces.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PurchaseOrderReceivedIntegrationEvent(
        UUID purchaseOrderId,
        UUID tenantId,
        UUID supplierId,
        BigDecimal totalCost,
        Instant occurredOn
) {
}
