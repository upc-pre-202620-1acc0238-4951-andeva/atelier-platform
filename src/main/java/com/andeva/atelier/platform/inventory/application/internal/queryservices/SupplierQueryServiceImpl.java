package com.andeva.atelier.platform.inventory.application.internal.queryservices;

import com.andeva.atelier.platform.inventory.application.queryservices.SupplierQueryService;
import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSupplierByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSuppliersByTenantIdQuery;
import com.andeva.atelier.platform.inventory.domain.repositories.SupplierRepository;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.assemblers.SupplierPersistenceAssembler;
import com.andeva.atelier.platform.inventory.infrastructure.persistence.jpa.repositories.SupplierPersistenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SupplierQueryServiceImpl implements SupplierQueryService {

    private final SupplierRepository supplierRepository;
    private final SupplierPersistenceRepository supplierPersistenceRepository;

    public SupplierQueryServiceImpl(
            SupplierRepository supplierRepository,
            SupplierPersistenceRepository supplierPersistenceRepository
    ) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository, "supplierRepository cannot be null");
        this.supplierPersistenceRepository = Objects.requireNonNull(supplierPersistenceRepository, "supplierPersistenceRepository cannot be null");
    }

    @Override
    public Optional<Supplier> handle(GetSupplierByIdQuery query) {
        Objects.requireNonNull(query, "GetSupplierByIdQuery cannot be null");
        return supplierRepository.findById(query.supplierId());
    }

    @Override
    public List<Supplier> handle(GetSuppliersByTenantIdQuery query) {
        Objects.requireNonNull(query, "GetSuppliersByTenantIdQuery cannot be null");
        if (query.search() != null || query.activeOnly() != null) {
            return supplierPersistenceRepository.searchSuppliers(
                    query.tenantId().value(),
                    query.search(),
                    query.activeOnly()
            ).stream().map(SupplierPersistenceAssembler::toDomain).toList();
        }
        return supplierRepository.findByTenantId(query.tenantId());
    }
}
