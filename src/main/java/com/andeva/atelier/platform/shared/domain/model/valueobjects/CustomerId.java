package com.andeva.atelier.platform.shared.domain.model.valueobjects;


import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for the Customer (individual or fleet).
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerId(UUID value) implements Serializable {
    public CustomerId {
        Objects.requireNonNull(value, "Customer identifier cannot be null");
    }

    public static CustomerId of(UUID value) {
        return new CustomerId(value);
    }

    public static CustomerId of(String value) {
        Objects.requireNonNull(value, "Customer identifier cannot be null");
        return new CustomerId(UUID.fromString(value));
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }
}
