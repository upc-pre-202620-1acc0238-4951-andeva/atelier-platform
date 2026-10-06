package com.andeva.atelier.platform.inventory.domain.exceptions;

import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;

public class PurchaseOrderNotFoundException extends InventoryDomainException {
    public PurchaseOrderNotFoundException(PurchaseOrderId id) {
        super("PurchaseOrder not found: " + (id != null ? id.value() : "null"));
    }

    public PurchaseOrderNotFoundException(String message) {
        super(message);
    }
}
