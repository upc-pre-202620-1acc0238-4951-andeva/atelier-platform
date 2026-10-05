package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class CustomerInactiveException extends CrmDomainException {

    public CustomerInactiveException(UUID customerId) {
        super("CUSTOMER_INACTIVE", String.format("Customer %s is inactive and cannot perform commercial operations", customerId));
    }
}
