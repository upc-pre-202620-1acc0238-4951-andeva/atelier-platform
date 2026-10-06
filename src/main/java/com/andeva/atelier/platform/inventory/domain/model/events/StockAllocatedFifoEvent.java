package com.andeva.atelier.platform.inventory.domain.model.events;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record StockAllocatedFifoEvent(
        InventoryItemId itemId,
        Quantity allocatedQuantity,
        Money totalCostOfGoodsSold,
        Instant occurredOn
) implements Serializable {

    public StockAllocatedFifoEvent {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(allocatedQuantity, "allocatedQuantity cannot be null");
        Objects.requireNonNull(totalCostOfGoodsSold, "totalCostOfGoodsSold cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static StockAllocatedFifoEvent of(InventoryItemId itemId, Quantity allocatedQuantity, Money totalCostOfGoodsSold) {
        return new StockAllocatedFifoEvent(itemId, allocatedQuantity, totalCostOfGoodsSold, Instant.now());
    }
}
