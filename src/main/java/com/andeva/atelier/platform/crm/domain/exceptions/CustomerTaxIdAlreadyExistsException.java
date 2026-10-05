package com.andeva.atelier.platform.crm.domain.exceptions;

public class CustomerTaxIdAlreadyExistsException extends CrmDomainException {
    public CustomerTaxIdAlreadyExistsException(String taxId) {
        super("CUSTOMER_TAX_ID_ALREADY_EXISTS", String.format("Ya existe un cliente registrado con ese documento de identidad en este taller: %s", taxId));
    }
}
