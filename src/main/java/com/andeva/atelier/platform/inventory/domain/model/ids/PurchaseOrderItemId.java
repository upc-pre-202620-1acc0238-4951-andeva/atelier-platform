package com.andeva.atelier.platform.inventory.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record PurchaseOrderItemId(UUID value) implements Serializable {

    public PurchaseOrderItemId {
        Objects.requireNonNull(value, "PurchaseOrderItemId value cannot be null");
    }

    public static PurchaseOrderItemId generate() {
        return new PurchaseOrderItemId(UUID.randomUUID());
    }

    public static PurchaseOrderItemId of(UUID value) {
        return new PurchaseOrderItemId(value);
    }
}
