package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record AllocateStockFifoCommand(
        InventoryItemId itemId,
        Quantity requestedQuantity,
        UUID workOrderId,
        UUID taskId
) implements Serializable {

    public AllocateStockFifoCommand {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(requestedQuantity, "requestedQuantity cannot be null");
    }

    public AllocateStockFifoCommand(InventoryItemId itemId, Quantity requestedQuantity) {
        this(itemId, requestedQuantity, null, null);
    }
}
