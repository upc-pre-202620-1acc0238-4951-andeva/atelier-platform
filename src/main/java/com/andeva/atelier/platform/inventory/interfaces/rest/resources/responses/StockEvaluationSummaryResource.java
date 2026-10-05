package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;

public record StockEvaluationSummaryResource(
        int totalItemsEvaluated,
        int lowStockItemsCount,
        BigDecimal totalInventoryValuation,
        Instant evaluatedAt
) {
}
