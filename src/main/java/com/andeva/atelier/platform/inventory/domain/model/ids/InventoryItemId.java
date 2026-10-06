package com.andeva.atelier.platform.inventory.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record InventoryItemId(UUID value) implements Serializable {

    public InventoryItemId {
        Objects.requireNonNull(value, "InventoryItemId value cannot be null");
    }

    public static InventoryItemId generate() {
        return new InventoryItemId(UUID.randomUUID());
    }

    public static InventoryItemId of(UUID value) {
        return new InventoryItemId(value);
    }
}
