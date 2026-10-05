package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetInventoryItemBySkuQuery(
        TenantId tenantId,
        Sku sku
) {
    public GetInventoryItemBySkuQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(sku, "sku cannot be null");
    }
}
