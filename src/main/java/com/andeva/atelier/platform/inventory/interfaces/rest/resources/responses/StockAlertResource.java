package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StockAlertResource(
        UUID itemId,
        String name,
        String sku,
        ItemCategory category,
        BigDecimal currentStock,
        BigDecimal minimumStock,
        BigDecimal deficit,
        Instant evaluatedAt
) {
}
