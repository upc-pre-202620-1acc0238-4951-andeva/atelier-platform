package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Fuel tank level reading represented as a percentage (0.0% to 100.0%).
 *
 * @author Joel Huamani Estefanero
 */
public record FuelLevel(double percentage) implements Serializable {

    public static final double MIN_PERCENTAGE = 0.0;
    public static final double MAX_PERCENTAGE = 100.0;
    public static final double LOW_FUEL_THRESHOLD = 15.0;
    public static final double CRITICAL_FUEL_THRESHOLD = 5.0;

    public FuelLevel {
        if (Double.isNaN(percentage) || percentage < MIN_PERCENTAGE || percentage > MAX_PERCENTAGE) {
            throw new IllegalArgumentException(
                    String.format("Fuel level percentage must be between %.1f and %.1f: %.2f", MIN_PERCENTAGE, MAX_PERCENTAGE, percentage)
            );
        }
    }

    public static FuelLevel of(double percentage) {
        return new FuelLevel(percentage);
    }

    public boolean isLowFuel() {
        return percentage <= LOW_FUEL_THRESHOLD;
    }

    public boolean isCriticallyLow() {
        return percentage <= CRITICAL_FUEL_THRESHOLD;
    }

    @Override
    public String toString() {
        return String.format("%.1f%%", percentage);
    }
}
