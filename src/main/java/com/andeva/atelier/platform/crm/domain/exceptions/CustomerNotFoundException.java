package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class CustomerNotFoundException extends CrmDomainException {

    public CustomerNotFoundException(UUID customerId) {
        super("CUSTOMER_NOT_FOUND", String.format("Customer with identifier %s was not found", customerId));
    }

    public CustomerNotFoundException(String taxId) {
        super("CUSTOMER_NOT_FOUND", String.format("Customer with tax ID %s was not found", taxId));
    }
}
