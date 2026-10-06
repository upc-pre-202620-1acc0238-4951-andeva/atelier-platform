package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record InventoryItemDeactivatedEvent(
        InventoryItemId itemId,
        TenantId tenantId,
        Instant occurredOn
) implements Serializable {

    public InventoryItemDeactivatedEvent {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static InventoryItemDeactivatedEvent of(InventoryItemId itemId, TenantId tenantId) {
        return new InventoryItemDeactivatedEvent(itemId, tenantId, Instant.now());
    }
}
