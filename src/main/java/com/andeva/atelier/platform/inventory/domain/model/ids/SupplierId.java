package com.andeva.atelier.platform.inventory.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record SupplierId(UUID value) implements Serializable {

    public SupplierId {
        Objects.requireNonNull(value, "SupplierId value cannot be null");
    }

    public static SupplierId generate() {
        return new SupplierId(UUID.randomUUID());
    }

    public static SupplierId of(UUID value) {
        return new SupplierId(value);
    }
}
