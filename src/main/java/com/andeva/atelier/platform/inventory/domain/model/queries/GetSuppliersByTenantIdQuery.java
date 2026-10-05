package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetSuppliersByTenantIdQuery(
        TenantId tenantId,
        String search,
        Boolean activeOnly
) {
    public GetSuppliersByTenantIdQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }
}
