package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetInventoryValuationQuery(
        TenantId tenantId
) {
    public GetInventoryValuationQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }
}
