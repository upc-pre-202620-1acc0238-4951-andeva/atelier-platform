package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;

import java.io.Serializable;
import java.util.Objects;

public record DeactivateInventoryItemCommand(InventoryItemId itemId) implements Serializable {
    public DeactivateInventoryItemCommand {
        Objects.requireNonNull(itemId, "itemId cannot be null");
    }
}
