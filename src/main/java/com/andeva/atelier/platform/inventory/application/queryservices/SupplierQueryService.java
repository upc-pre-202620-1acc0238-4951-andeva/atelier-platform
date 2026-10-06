package com.andeva.atelier.platform.inventory.application.queryservices;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSupplierByIdQuery;
import com.andeva.atelier.platform.inventory.domain.model.queries.GetSuppliersByTenantIdQuery;

import java.util.List;
import java.util.Optional;

public interface SupplierQueryService {

    Optional<Supplier> handle(GetSupplierByIdQuery query);

    List<Supplier> handle(GetSuppliersByTenantIdQuery query);
}
