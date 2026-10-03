package com.andeva.atelier.platform.crm.domain.repositories;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Outbound persistence port for the Customer aggregate.
 */
public interface CustomerRepository {

    Customer save(Customer customer);

    Optional<Customer> findById(CustomerId id);

    Optional<Customer> findByIdAndTenantId(CustomerId id, TenantId tenantId);

    Optional<Customer> findByTenantIdAndTaxId(TenantId tenantId, String taxId);

    List<Customer> search(TenantId tenantId, CustomerType type, String searchTerm, CustomerStatus status);

    boolean existsByTenantIdAndTaxId(TenantId tenantId, String taxId);

    boolean existsByTenantIdAndEmail(TenantId tenantId, String email);
}
