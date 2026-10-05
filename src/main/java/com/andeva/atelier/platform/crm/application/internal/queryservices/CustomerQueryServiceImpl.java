package com.andeva.atelier.platform.crm.application.internal.queryservices;

import com.andeva.atelier.platform.crm.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByTaxIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomersByTenantIdQuery;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of CustomerQueryService executing read-only projections.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(readOnly = true)
public class CustomerQueryServiceImpl implements CustomerQueryService {

    private final CustomerRepository customerRepository;

    public CustomerQueryServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null");
    }

    @Override
    public Optional<Customer> handle(GetCustomerByIdQuery query) {
        Objects.requireNonNull(query, "GetCustomerByIdQuery cannot be null");
        return customerRepository.findByIdAndTenantId(query.customerId(), query.tenantId());
    }

    @Override
    public List<Customer> handle(GetCustomersByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetCustomersByTenantIdQuery cannot be null");
        return customerRepository.search(query.tenantId(), query.type(), query.search(), query.status());
    }

    @Override
    public Optional<Customer> handle(GetCustomerByTaxIdQuery query) {
        Objects.requireNonNull(query, "GetCustomerByTaxIdQuery cannot be null");
        return customerRepository.findByTenantIdAndTaxId(query.tenantId(), query.taxId());
    }

    @Override
    public Optional<Customer> handle(com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByDocumentQuery query) {
        Objects.requireNonNull(query, "GetCustomerByDocumentQuery cannot be null");
        return customerRepository.findByTenantIdAndTaxId(query.tenantId(), query.documentNumber());
    }
}
