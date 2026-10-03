package com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.assemblers.CustomerPersistenceAssembler;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.entities.CustomerPersistenceEntity;
import com.andeva.atelier.platform.crm.infrastructure.persistence.jpa.repositories.CustomerPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JPA adapter implementing the CustomerRepository domain port.
 *
 * @author Adiel Sanchez Santin
 */
@Repository
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerPersistenceRepository customerPersistenceRepository;

    public CustomerRepositoryImpl(CustomerPersistenceRepository customerPersistenceRepository) {
        this.customerPersistenceRepository = Objects.requireNonNull(customerPersistenceRepository);
    }

    @Override
    public Customer save(Customer customer) {
        CustomerPersistenceEntity entity = customerPersistenceRepository.findById(customer.id().value())
                .map(existing -> {
                    CustomerPersistenceAssembler.updateEntity(existing, customer);
                    return existing;
                })
                .orElseGet(() -> CustomerPersistenceAssembler.toEntity(customer));
        CustomerPersistenceEntity saved = customerPersistenceRepository.save(entity);
        return CustomerPersistenceAssembler.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return customerPersistenceRepository.findById(id.value())
                .map(CustomerPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Customer> findByIdAndTenantId(CustomerId id, TenantId tenantId) {
        return customerPersistenceRepository.findByIdAndTenantId(id.value(), tenantId.value())
                .map(CustomerPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Customer> findByTenantIdAndTaxId(TenantId tenantId, String taxId) {
        return customerPersistenceRepository.findByTenantIdAndTaxId(tenantId.value(), taxId)
                .map(CustomerPersistenceAssembler::toDomain);
    }

    @Override
    public List<Customer> search(TenantId tenantId, CustomerType type, String searchTerm, CustomerStatus status) {
        String statusStr = status != null ? status.name() : null;
        return customerPersistenceRepository.searchCustomers(tenantId.value(), type, searchTerm, statusStr)
                .stream()
                .map(CustomerPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public boolean existsByTenantIdAndTaxId(TenantId tenantId, String taxId) {
        return customerPersistenceRepository.existsByTenantIdAndTaxId(tenantId.value(), taxId);
    }

    @Override
    public boolean existsByTenantIdAndEmail(TenantId tenantId, String email) {
        return customerPersistenceRepository.existsByTenantIdAndEmail(tenantId.value(), email);
    }
}
