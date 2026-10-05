package com.andeva.atelier.platform.crm.domain.exceptions;

public class CustomerAlreadyExistsException extends CrmDomainException {

    public CustomerAlreadyExistsException(String taxId) {
        super("CUSTOMER_ALREADY_EXISTS", String.format("A customer with tax ID %s already exists in this workshop", taxId));
    }
}
