package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import java.io.Serializable;

public record HaversineDistance(double meters) implements Serializable {

    public HaversineDistance {
        if (meters < 0.0) {
            throw new IllegalArgumentException("HaversineDistance cannot be negative: " + meters);
        }
    }

    public static HaversineDistance of(double meters) {
        return new HaversineDistance(meters);
    }

    public boolean isWithin(double thresholdMeters) {
        return this.meters <= thresholdMeters;
    }

    public double toKilometers() {
        return this.meters / 1000.0;
    }
}
