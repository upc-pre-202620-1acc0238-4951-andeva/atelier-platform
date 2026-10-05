package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

public record StorageUrl(String value) implements Serializable {

    public StorageUrl {
        Objects.requireNonNull(value, "StorageUrl value cannot be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty() || (!trimmed.startsWith("https://") && !trimmed.startsWith("gs://"))) {
            throw new IllegalArgumentException("StorageUrl must be a valid secure URL: " + value);
        }
    }

    public static StorageUrl of(String value) {
        return new StorageUrl(value);
    }
}
