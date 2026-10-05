package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

public record Mileage(Integer value) implements Serializable {

    public Mileage {
        Objects.requireNonNull(value, "Mileage value cannot be null");
        if (value < 0) {
            throw new IllegalArgumentException("Mileage cannot be negative: " + value);
        }
    }

    public static Mileage of(Integer value) {
        return new Mileage(value);
    }

    public static Mileage zero() {
        return new Mileage(0);
    }
}
