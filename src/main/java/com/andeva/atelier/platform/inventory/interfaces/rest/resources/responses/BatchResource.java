package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record BatchResource(
        UUID id,
        UUID itemId,
        UUID supplierId,
        UUID purchaseOrderId,
        String batchNumber,
        BigDecimal initialQty,
        BigDecimal remainingQty,
        BigDecimal unitCost,
        Instant arrivalDate,
        String receiptImageUrl
) {
}
