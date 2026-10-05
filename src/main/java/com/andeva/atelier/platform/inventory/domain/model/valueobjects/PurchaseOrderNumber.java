package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

public record PurchaseOrderNumber(String value) implements Serializable {

    public PurchaseOrderNumber {
        Objects.requireNonNull(value, "PurchaseOrderNumber value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PurchaseOrderNumber value cannot be blank");
        }
    }

    public static PurchaseOrderNumber of(String value) {
        return new PurchaseOrderNumber(value);
    }
}
