package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Units of an item withdrawn from a work order task go back to the batches they were taken from (US33).
 *
 * @param returnedBy user who removed the part from the task (audit)
 */
public record ReturnWorkOrderPartsCommand(
        InventoryItemId itemId,
        UUID workOrderId,
        UUID taskId,
        Quantity quantity,
        UUID returnedBy,
        String reason
) implements Serializable {

    public ReturnWorkOrderPartsCommand {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
    }
}
