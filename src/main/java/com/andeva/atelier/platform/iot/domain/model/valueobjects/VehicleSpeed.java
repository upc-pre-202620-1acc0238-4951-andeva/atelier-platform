package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Instantaneous vehicle linear speed measured in kilometers per hour (km/h).
 * Enforces valid automotive velocity range [0, 350].
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleSpeed(int kmh) implements Serializable {

    public static final int MIN_SPEED_KMH = 0;
    public static final int MAX_SPEED_KMH = 350;

    public VehicleSpeed {
        if (kmh < MIN_SPEED_KMH || kmh > MAX_SPEED_KMH) {
            throw new IllegalArgumentException(
                    String.format("Vehicle speed must be between %d km/h and %d km/h: %d", MIN_SPEED_KMH, MAX_SPEED_KMH, kmh)
            );
        }
    }

    public static VehicleSpeed of(int kmh) {
        return new VehicleSpeed(kmh);
    }

    public static VehicleSpeed of(double kmh) {
        return new VehicleSpeed((int) Math.round(kmh));
    }

    public boolean isMoving() {
        return kmh > 0;
    }

    public boolean isStationary() {
        return kmh == 0;
    }

    public boolean isExcessiveSpeed(int limitKmh) {
        return kmh > limitKmh;
    }

    public double toMph() {
        return kmh * 0.621371;
    }

    @Override
    public String toString() {
        return kmh + " km/h";
    }
}
