package com.andeva.atelier.platform.crm.application.queryservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByTaxIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomersByTenantIdQuery;

import java.util.List;
import java.util.Optional;

/**
 * Public application port for customer read queries.
 *
 * @author Adiel Sanchez Santin
 */
public interface CustomerQueryService {

    Optional<Customer> handle(GetCustomerByIdQuery query);

    List<Customer> handle(GetCustomersByTenantIdQuery query);

    Optional<Customer> handle(GetCustomerByTaxIdQuery query);
}
