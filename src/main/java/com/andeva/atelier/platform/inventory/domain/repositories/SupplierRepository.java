package com.andeva.atelier.platform.inventory.domain.repositories;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

public interface SupplierRepository {

    Optional<Supplier> findById(SupplierId id);

    Optional<Supplier> findByTenantIdAndTaxId(TenantId tenantId, TaxId taxId);

    boolean existsByTenantIdAndTaxId(TenantId tenantId, TaxId taxId);

    List<Supplier> findByTenantId(TenantId tenantId);

    List<Supplier> findByTenantIdAndActive(TenantId tenantId, boolean active);

    Supplier save(Supplier supplier);
}
