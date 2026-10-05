package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

public record StorageUrl(String value) implements Serializable {

    public StorageUrl {
        Objects.requireNonNull(value, "StorageUrl value cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("StorageUrl value cannot be blank");
        }
    }

    public static StorageUrl of(String value) {
        return new StorageUrl(value);
    }
}
