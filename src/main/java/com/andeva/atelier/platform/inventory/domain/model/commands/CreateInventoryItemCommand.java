package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

public record CreateInventoryItemCommand(
        TenantId tenantId,
        String name,
        Sku sku,
        ItemCategory category,
        Money basePrice,
        Quantity minimumStock,
        String unitOfMeasure
) implements Serializable {

    public CreateInventoryItemCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(sku, "sku cannot be null");
        Objects.requireNonNull(category, "category cannot be null");
        Objects.requireNonNull(basePrice, "basePrice cannot be null");
    }
}
