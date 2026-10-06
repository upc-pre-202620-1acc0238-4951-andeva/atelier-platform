package com.andeva.atelier.platform.inventory.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record PurchaseOrderId(UUID value) implements Serializable {

    public PurchaseOrderId {
        Objects.requireNonNull(value, "PurchaseOrderId value cannot be null");
    }

    public static PurchaseOrderId generate() {
        return new PurchaseOrderId(UUID.randomUUID());
    }

    public static PurchaseOrderId of(UUID value) {
        return new PurchaseOrderId(value);
    }
}
