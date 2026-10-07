package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Mathematical probability score (0.00% to 100.00%) estimated by analytical or AI inference engines.
 *
 * @author Joel Huamani Estefanero
 */
public record ConfidenceScore(BigDecimal value) implements Serializable {

    private static final BigDecimal MIN_VALUE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
    private static final BigDecimal MAX_VALUE = new BigDecimal("100.00");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("85.00");
    private static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("95.00");

    public ConfidenceScore {
        Objects.requireNonNull(value, "Confidence score value cannot be null");
        BigDecimal scaled = value.setScale(2, RoundingMode.HALF_EVEN);
        if (scaled.compareTo(MIN_VALUE) < 0 || scaled.compareTo(MAX_VALUE) > 0) {
            throw new IllegalArgumentException("Confidence score must be between 0.00 and 100.00: " + scaled);
        }
        value = scaled;
    }

    public static ConfidenceScore of(BigDecimal value) {
        return new ConfidenceScore(value);
    }

    public static ConfidenceScore of(double value) {
        return new ConfidenceScore(BigDecimal.valueOf(value));
    }

    public static ConfidenceScore of(int value) {
        return new ConfidenceScore(BigDecimal.valueOf(value));
    }

    public boolean isHighConfidence() {
        return value.compareTo(HIGH_THRESHOLD) >= 0;
    }

    public boolean isCriticalConfidence() {
        return value.compareTo(CRITICAL_THRESHOLD) >= 0;
    }

    public double percentage() {
        return value.doubleValue();
    }

    @Override
    public String toString() {
        return value.toPlainString() + "%";
    }
}
