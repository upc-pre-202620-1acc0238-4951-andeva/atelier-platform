package com.andeva.atelier.platform.inventory.domain.exceptions;

public class InvalidPurchaseOrderTransitionException extends InventoryDomainException {
    public InvalidPurchaseOrderTransitionException(String message) {
        super(message);
    }
}
