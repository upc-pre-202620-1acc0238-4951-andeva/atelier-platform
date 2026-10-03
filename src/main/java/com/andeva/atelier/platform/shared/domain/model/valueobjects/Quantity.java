package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object representing physical quantities for inventory items
 * or maintenance labor time units.
 *
 * @author Joel Huamani Estefanero
 */
public record Quantity(BigDecimal value, MeasurementUnit unit) {

    public Quantity {
        Objects.requireNonNull(value, "Quantitative value cannot be null");
        Objects.requireNonNull(unit, "Measurement unit cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + value);
        }
        value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Quantity of(BigDecimal value, MeasurementUnit unit) {
        return new Quantity(value, unit);
    }

    public static Quantity of(double value, MeasurementUnit unit) {
        return new Quantity(BigDecimal.valueOf(value), unit);
    }

    public static Quantity ofUnits(int count) {
        return new Quantity(BigDecimal.valueOf(count), MeasurementUnit.UNIT);
    }

    public Quantity add(Quantity other) {
        validateSameUnit(other);
        return new Quantity(this.value.add(other.value), this.unit);
    }

    public Quantity subtract(Quantity other) {
        validateSameUnit(other);
        if (this.value.compareTo(other.value) < 0) {
            throw new IllegalArgumentException(String.format(
                    "Insufficient stock to deduct: current %s, required %s",
                    this.value.toPlainString(),
                    other.value.toPlainString()));
        }
        return new Quantity(this.value.subtract(other.value), this.unit);
    }

    public boolean hasSufficient(Quantity required) {
        validateSameUnit(required);
        return this.value.compareTo(required.value) >= 0;
    }

    private void validateSameUnit(Quantity other) {
        Objects.requireNonNull(other, "Comparison quantity cannot be null");
        if (this.unit != other.unit) {
            throw new IllegalArgumentException(String.format("Measurement unit mismatch: %s vs %s", this.unit, other.unit));
        }
    }
}
