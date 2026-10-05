package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.enums.InventoryItemStatus;
import com.andeva.atelier.platform.inventory.domain.model.enums.ItemCategory;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetInventoryItemsPagedQuery(
        TenantId tenantId,
        int page,
        int size,
        String search,
        ItemCategory category,
        InventoryItemStatus status
) {
    public GetInventoryItemsPagedQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
    }
}
