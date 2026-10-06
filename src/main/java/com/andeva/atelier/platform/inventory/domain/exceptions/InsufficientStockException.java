package com.andeva.atelier.platform.inventory.domain.exceptions;

public class InsufficientStockException extends InventoryDomainException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
