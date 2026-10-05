package com.andeva.atelier.platform.inventory.domain.exceptions;

public class DuplicateSkuException extends InventoryDomainException {
    public DuplicateSkuException(String sku) {
        super("SKU already exists: " + sku);
    }
}
