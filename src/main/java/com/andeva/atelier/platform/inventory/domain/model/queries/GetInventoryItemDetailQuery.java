package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;
import java.util.UUID;

public record GetInventoryItemDetailQuery(
        TenantId tenantId,
        InventoryItemId itemId
) {
    public GetInventoryItemDetailQuery {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(itemId, "itemId cannot be null");
    }

    public GetInventoryItemDetailQuery(InventoryItemId itemId) {
        this(TenantId.of(UUID.randomUUID()), itemId);
    }
}
