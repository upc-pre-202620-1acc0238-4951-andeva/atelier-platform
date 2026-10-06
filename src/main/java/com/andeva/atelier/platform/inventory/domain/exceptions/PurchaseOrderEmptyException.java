package com.andeva.atelier.platform.inventory.domain.exceptions;

public class PurchaseOrderEmptyException extends InventoryDomainException {
    public PurchaseOrderEmptyException(String message) {
        super(message);
    }
}
