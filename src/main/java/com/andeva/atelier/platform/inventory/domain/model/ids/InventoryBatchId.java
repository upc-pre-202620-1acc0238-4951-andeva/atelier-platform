package com.andeva.atelier.platform.inventory.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record InventoryBatchId(UUID value) implements Serializable {

    public InventoryBatchId {
        Objects.requireNonNull(value, "InventoryBatchId value cannot be null");
    }

    public static InventoryBatchId generate() {
        return new InventoryBatchId(UUID.randomUUID());
    }

    public static InventoryBatchId of(UUID value) {
        return new InventoryBatchId(value);
    }
}
