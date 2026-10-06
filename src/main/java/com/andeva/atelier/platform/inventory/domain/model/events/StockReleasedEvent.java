package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record StockReleasedEvent(
        InventoryItemId itemId,
        Quantity releasedQuantity,
        Instant occurredOn
) implements Serializable {

    public StockReleasedEvent {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(releasedQuantity, "releasedQuantity cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static StockReleasedEvent of(InventoryItemId itemId, Quantity releasedQuantity) {
        return new StockReleasedEvent(itemId, releasedQuantity, Instant.now());
    }
}
