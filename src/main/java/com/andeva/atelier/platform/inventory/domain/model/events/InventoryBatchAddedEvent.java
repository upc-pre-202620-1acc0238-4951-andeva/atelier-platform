package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record InventoryBatchAddedEvent(
        InventoryBatchId batchId,
        InventoryItemId itemId,
        Quantity quantity,
        Money unitCost,
        Instant occurredOn
) implements Serializable {

    public InventoryBatchAddedEvent {
        Objects.requireNonNull(batchId, "batchId cannot be null");
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(unitCost, "unitCost cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static InventoryBatchAddedEvent of(InventoryBatchId batchId, InventoryItemId itemId, Quantity quantity, Money unitCost) {
        return new InventoryBatchAddedEvent(batchId, itemId, quantity, unitCost, Instant.now());
    }
}
