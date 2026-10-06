package com.andeva.atelier.platform.inventory.domain.services;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Domain service calculating reorder point evaluation and recommended restock quantities.
 */
public class StockReorderEvaluationService {

    public boolean isReorderRequired(InventoryItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        return item.getTotalStock().isLessThanOrEqualTo(item.getMinimumStock());
    }

    public Quantity calculateSuggestedReorderQuantity(InventoryItem item) {
        Objects.requireNonNull(item, "item cannot be null");
        if (!isReorderRequired(item)) {
            return Quantity.ZERO;
        }
        // Basic reorder policy: double the minimum stock minus current stock, or at least minimum stock
        Quantity deficit = item.getMinimumStock().subtract(item.getTotalStock());
        Quantity suggested = item.getMinimumStock().add(deficit);
        return suggested.value().compareTo(BigDecimal.ZERO) > 0 ? suggested : Quantity.of(BigDecimal.ONE);
    }
}
