package com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.inventory.domain.repositories.SupplierRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.SupplierPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.entities.SupplierPersistenceEntity;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.SupplierPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class SupplierRepositoryImpl implements SupplierRepository {

    private final SupplierPersistenceRepository supplierRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SupplierRepositoryImpl(
            SupplierPersistenceRepository supplierRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "supplierRepository cannot be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher cannot be null");
    }

    @Override
    public Optional<Supplier> findById(SupplierId id) {
        Objects.requireNonNull(id, "id cannot be null");
        return supplierRepository.findById(id.value())
                .map(SupplierPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<Supplier> findByTenantIdAndTaxId(TenantId tenantId, TaxId taxId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(taxId, "taxId cannot be null");
        return supplierRepository.findByTenantIdAndTaxId(tenantId.value(), taxId.value())
                .map(SupplierPersistenceAssembler::toDomain);
    }

    @Override
    public boolean existsByTenantIdAndTaxId(TenantId tenantId, TaxId taxId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(taxId, "taxId cannot be null");
        return supplierRepository.existsByTenantIdAndTaxId(tenantId.value(), taxId.value());
    }

    @Override
    public List<Supplier> findByTenantId(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return supplierRepository.findByTenantId(tenantId.value()).stream()
                .map(SupplierPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public List<Supplier> findByTenantIdAndActive(TenantId tenantId, boolean active) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return supplierRepository.findByTenantIdAndActive(tenantId.value(), active).stream()
                .map(SupplierPersistenceAssembler::toDomain)
                .toList();
    }

    @Override
    public Supplier save(Supplier supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        SupplierPersistenceEntity entity = supplierRepository.findById(supplier.getId().value())
                .map(existing -> {
                    SupplierPersistenceAssembler.updateEntity(existing, supplier);
                    return existing;
                })
                .orElseGet(() -> SupplierPersistenceAssembler.toEntity(supplier));

        SupplierPersistenceEntity saved = supplierRepository.save(entity);
        Supplier domain = SupplierPersistenceAssembler.toDomain(saved);

        supplier.domainEvents().forEach(eventPublisher::publishEvent);
        supplier.clearDomainEvents();

        return domain;
    }

    @Override
    public void delete(Supplier supplier) {
        Objects.requireNonNull(supplier, "supplier cannot be null");
        supplierRepository.deleteById(supplier.getId().value());
    }
}
