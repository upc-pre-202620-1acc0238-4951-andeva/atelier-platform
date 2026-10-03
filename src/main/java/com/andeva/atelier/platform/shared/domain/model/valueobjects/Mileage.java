package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import java.util.Objects;

/**
 * Value Object representing the automotive odometer mileage traversed by a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record Mileage(int value) {

    public Mileage {
        if (value < 0) {
            throw new IllegalArgumentException("Automotive mileage cannot be negative: " + value);
        }
    }

    public static Mileage of(int km) {
        return new Mileage(km);
    }

    public boolean isGreaterThan(Mileage other) {
        Objects.requireNonNull(other, "Comparison mileage cannot be null");
        return this.value > other.value;
    }

    public int difference(Mileage other) {
        Objects.requireNonNull(other, "Comparison mileage cannot be null");
        return Math.abs(this.value - other.value);
    }

    @Override
    public String toString() {
        return value + " km";
    }
}
