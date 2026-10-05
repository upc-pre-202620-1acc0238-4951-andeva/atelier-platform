package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryBatchId;
import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StockAllocation;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record ReleaseStockAllocationCommand(
        InventoryItemId itemId,
        StockAllocation allocation,
        InventoryBatchId batchId,
        Quantity quantity,
        UUID workOrderId,
        String reason
) implements Serializable {

    public ReleaseStockAllocationCommand {
        Objects.requireNonNull(itemId, "itemId cannot be null");
    }

    public static ReleaseStockAllocationCommand ofAllocation(InventoryItemId itemId, StockAllocation allocation) {
        return new ReleaseStockAllocationCommand(itemId, Objects.requireNonNull(allocation, "allocation cannot be null"), null, null, null, null);
    }

    public static ReleaseStockAllocationCommand ofBatch(InventoryItemId itemId, InventoryBatchId batchId, Quantity quantity, UUID workOrderId, String reason) {
        return new ReleaseStockAllocationCommand(itemId, null, Objects.requireNonNull(batchId, "batchId cannot be null"), Objects.requireNonNull(quantity, "quantity cannot be null"), workOrderId, reason);
    }
}
