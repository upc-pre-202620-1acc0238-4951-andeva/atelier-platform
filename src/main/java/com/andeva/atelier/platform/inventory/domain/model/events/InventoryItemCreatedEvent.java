package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Sku;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record InventoryItemCreatedEvent(
        InventoryItemId itemId,
        TenantId tenantId,
        Sku sku,
        String name,
        Instant occurredOn
) implements Serializable {

    public InventoryItemCreatedEvent {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(sku, "sku cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static InventoryItemCreatedEvent of(InventoryItemId itemId, TenantId tenantId, Sku sku, String name) {
        return new InventoryItemCreatedEvent(itemId, tenantId, sku, name, Instant.now());
    }
}
