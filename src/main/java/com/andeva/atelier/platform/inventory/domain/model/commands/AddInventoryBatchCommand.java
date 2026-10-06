package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;
import com.andeva.atelier.platform.inventory.domain.model.ids.PurchaseOrderId;
import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.inventory.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record AddInventoryBatchCommand(
        InventoryItemId itemId,
        SupplierId supplierId,
        PurchaseOrderId purchaseOrderId,
        String batchNumber,
        Quantity quantity,
        Money unitCost,
        Instant arrivalDate,
        StorageUrl receiptImageUrl
) implements Serializable {

    public AddInventoryBatchCommand {
        Objects.requireNonNull(itemId, "itemId cannot be null");
        Objects.requireNonNull(batchNumber, "batchNumber cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(unitCost, "unitCost cannot be null");
    }
}
