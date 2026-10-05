package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResource(
        UUID id,
        UUID itemId,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalCost
) {
}
