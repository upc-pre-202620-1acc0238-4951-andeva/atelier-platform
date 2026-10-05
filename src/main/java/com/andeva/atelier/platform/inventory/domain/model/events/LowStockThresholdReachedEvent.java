package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record LowStockThresholdReachedEvent(
        InventoryItemId itemId,
        TenantId tenantId,
        Quantity currentStock,
        Quantity minimumStock,
        Instant occurredOn
) implements Serializable {

    public LowStockThresholdReachedEvent {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(currentStock, "currentStock cannot be null");
        Objects.requireNonNull(minimumStock, "minimumStock cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static LowStockThresholdReachedEvent of(InventoryItemId itemId, TenantId tenantId, Quantity currentStock, Quantity minimumStock) {
        return new LowStockThresholdReachedEvent(itemId, tenantId, currentStock, minimumStock, Instant.now());
    }
}
