package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryItemSummaryDto(
        UUID id,
        String sku,
        String name,
        BigDecimal currentStock,
        BigDecimal reservedStock,
        String unitOfMeasure,
        String currency
) {
}
