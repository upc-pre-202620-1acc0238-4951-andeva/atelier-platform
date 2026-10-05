package com.andeva.atelier.platform.crm.interfaces.rest.transform;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.responses.CustomerResource;

/**
 * Assembler transforming Customer domain aggregates into CustomerResource REST responses.
 *
 * @author Adiel Sanchez Santin
 */
public final class CustomerResourceFromAggregateAssembler {

    private CustomerResourceFromAggregateAssembler() {
    }

    public static CustomerResource toResourceFromEntity(Customer customer) {
        if (customer == null) {
            return null;
        }

        String firstName = customer.name() != null ? customer.name().firstName() : null;
        String lastName = customer.name() != null ? customer.name().lastName() : null;

        return new CustomerResource(
                customer.id().value(),
                customer.tenantId().value(),
                customer.type().name(),
                firstName,
                lastName,
                customer.companyName(),
                customer.getDisplayName(),
                customer.taxId().value(),
                customer.email() != null ? customer.email().value() : null,
                customer.phone() != null ? customer.phone().value() : null,
                customer.status().name()
        );
    }
}
