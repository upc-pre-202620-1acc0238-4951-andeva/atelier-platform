package com.andeva.atelier.platform.inventory.domain.exceptions;

public class InvalidBatchQuantityException extends InventoryDomainException {
    public InvalidBatchQuantityException(String message) {
        super(message);
    }
}
