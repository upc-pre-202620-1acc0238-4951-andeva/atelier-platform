package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetInventoryItemsByTenantIdQuery(
        TenantId tenantId,
        ItemCategory category,
        InventoryItemStatus status
) {
    public GetInventoryItemsByTenantIdQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    public GetInventoryItemsByTenantIdQuery(TenantId tenantId) {
        this(tenantId, null, null);
    }
}
