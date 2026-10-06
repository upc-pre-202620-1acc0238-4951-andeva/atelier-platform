package com.andeva.atelier.platform.inventory.domain.exceptions;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;

public class InventoryItemNotFoundException extends InventoryDomainException {
    public InventoryItemNotFoundException(InventoryItemId id) {
        super("InventoryItem not found: " + (id != null ? id.value() : "null"));
    }

    public InventoryItemNotFoundException(String message) {
        super(message);
    }
}
