package com.andeva.atelier.platform.inventory.domain.model.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Quantity(BigDecimal value) implements Serializable, Comparable<Quantity> {

    public static final Quantity ZERO = new Quantity(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));

    public Quantity {
        Objects.requireNonNull(value, "Quantity value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + value);
        }
        value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Quantity of(BigDecimal value) {
        return new Quantity(value);
    }

    public static Quantity of(double value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    public static Quantity of(int value) {
        return new Quantity(BigDecimal.valueOf(value));
    }

    public Quantity add(Quantity other) {
        Objects.requireNonNull(other, "other quantity cannot be null");
        return new Quantity(this.value.add(other.value));
    }

    public Quantity subtract(Quantity other) {
        Objects.requireNonNull(other, "other quantity cannot be null");
        if (this.isLessThan(other)) {
            throw new IllegalArgumentException("Cannot subtract " + other.value + " from " + this.value + " (result would be negative)");
        }
        return new Quantity(this.value.subtract(other.value));
    }

    public boolean isGreaterThan(Quantity other) {
        Objects.requireNonNull(other, "other quantity cannot be null");
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isGreaterThanOrEqualTo(Quantity other) {
        Objects.requireNonNull(other, "other quantity cannot be null");
        return this.value.compareTo(other.value) >= 0;
    }

    public boolean isLessThan(Quantity other) {
        Objects.requireNonNull(other, "other quantity cannot be null");
        return this.value.compareTo(other.value) < 0;
    }

    public boolean isLessThanOrEqualTo(Quantity other) {
        Objects.requireNonNull(other, "other quantity cannot be null");
        return this.value.compareTo(other.value) <= 0;
    }

    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public int compareTo(Quantity o) {
        return this.value.compareTo(o.value);
    }
}
