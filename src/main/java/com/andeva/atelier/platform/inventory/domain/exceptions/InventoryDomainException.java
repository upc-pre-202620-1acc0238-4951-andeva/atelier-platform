package com.andeva.atelier.platform.inventory.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

public class InventoryDomainException extends DomainException {

    public InventoryDomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    public InventoryDomainException(String message) {
        super("INVENTORY_DOMAIN_ERROR", message);
    }
}
