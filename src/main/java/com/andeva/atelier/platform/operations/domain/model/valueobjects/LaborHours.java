package com.andeva.atelier.platform.operations.domain.model.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record LaborHours(BigDecimal value) implements Serializable {

    public LaborHours {
        Objects.requireNonNull(value, "LaborHours value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("LaborHours cannot be negative: " + value);
        }
        value = value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static LaborHours of(BigDecimal value) {
        return new LaborHours(value);
    }

    public static LaborHours of(double value) {
        return new LaborHours(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN));
    }

    public static LaborHours zero() {
        return new LaborHours(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
    }
}
