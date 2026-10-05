package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public record Sku(String value) implements Serializable {

    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,50}$");

    public Sku {
        Objects.requireNonNull(value, "SKU value cannot be null");
        String normalized = value.trim().toUpperCase();
        if (!SKU_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid SKU format: " + value + ". Must be 3-50 alphanumeric characters.");
        }
        value = normalized;
    }

    public static Sku of(String value) {
        return new Sku(value);
    }
}
