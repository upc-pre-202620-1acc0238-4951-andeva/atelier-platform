package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.util.Objects;

public record UpdateInventoryItemCommand(
        InventoryItemId itemId,
        String name,
        ItemCategory category,
        Money basePrice,
        Quantity minimumStock,
        String unitOfMeasure,
        String status
) implements Serializable {

    public UpdateInventoryItemCommand {
        Objects.requireNonNull(itemId, "itemId cannot be null");
    }
}
