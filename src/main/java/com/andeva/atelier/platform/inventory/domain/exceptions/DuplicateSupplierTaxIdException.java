package com.andeva.atelier.platform.inventory.domain.exceptions;

public class DuplicateSupplierTaxIdException extends InventoryDomainException {
    public DuplicateSupplierTaxIdException(String taxId) {
        super("Supplier with TaxId already exists: " + taxId);
    }
}
