package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetLowStockItemsQuery(
        TenantId tenantId
) {
    public GetLowStockItemsQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }
}
