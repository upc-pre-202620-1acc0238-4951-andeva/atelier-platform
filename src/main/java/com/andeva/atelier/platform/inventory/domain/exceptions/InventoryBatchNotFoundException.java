package com.andeva.atelier.platform.inventory.domain.exceptions;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;

public class InventoryBatchNotFoundException extends InventoryDomainException {
    public InventoryBatchNotFoundException(InventoryBatchId id) {
        super("InventoryBatch not found: " + (id != null ? id.value() : "null"));
    }

    public InventoryBatchNotFoundException(String message) {
        super(message);
    }
}
