package com.andeva.atelier.platform.inventory.domain.exceptions;

import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;

public class SupplierNotFoundException extends InventoryDomainException {
    public SupplierNotFoundException(SupplierId id) {
        super("Supplier not found: " + (id != null ? id.value() : "null"));
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }
}
