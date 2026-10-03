package com.andeva.atelier.platform.shared.domain.model.valueobjects;

/**
 * Physical scalar separation magnitude in meters, used in geofencing validation.
 *
 * @author Joel Huamani Estefanero
 */
public record DistanceMeters(double value) {
    public DistanceMeters {
        if (value < 0.0) {
            throw new IllegalArgumentException("Distance in meters cannot be negative: " + value);
        }
    }

    public static DistanceMeters of(double meters) {
        return new DistanceMeters(meters);
    }

    public boolean isWithinThreshold(double thresholdMeters) {
        return this.value <= thresholdMeters;
    }
}
