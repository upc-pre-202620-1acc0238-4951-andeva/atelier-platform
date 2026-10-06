package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;

import java.math.BigDecimal;
import java.util.UUID;

public record PartResource(
        UUID id,
        UUID tenantId,
        String name,
        String sku,
        ItemCategory category,
        BigDecimal basePrice,
        BigDecimal totalStock,
        BigDecimal minimumStock,
        String unitOfMeasure,
        InventoryItemStatus status
) {
}
