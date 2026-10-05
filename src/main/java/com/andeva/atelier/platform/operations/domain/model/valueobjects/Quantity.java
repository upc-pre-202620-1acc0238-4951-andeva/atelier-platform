package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Quantity(BigDecimal value) implements Serializable {

    public Quantity {
        Objects.requireNonNull(value, "Quantity value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be strictly positive: " + value);
        }
        value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Quantity of(BigDecimal value) {
        return new Quantity(value);
    }

    public static Quantity of(double value) {
        return new Quantity(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN));
    }

    public static Quantity one() {
        return new Quantity(BigDecimal.ONE.setScale(2, RoundingMode.HALF_EVEN));
    }
}
