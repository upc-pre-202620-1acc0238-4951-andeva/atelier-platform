package com.andeva.atelier.platform.iot.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;

/**
 * Geographic GPS positioning coordinates (WGS84 datum) for vehicular tracking.
 * Latitude must be within [-90.0, 90.0] and Longitude within [-180.0, 180.0].
 *
 * @author Joel Huamani Estefanero
 */
public record GeoCoordinates(double latitude, double longitude) implements Serializable {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    public GeoCoordinates {
        if (Double.isNaN(latitude) || latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude out of WGS84 bounds [-90.0, 90.0]: " + latitude);
        }
        if (Double.isNaN(longitude) || longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude out of WGS84 bounds [-180.0, 180.0]: " + longitude);
        }
    }

    public static GeoCoordinates of(double latitude, double longitude) {
        return new GeoCoordinates(latitude, longitude);
    }

    /**
     * Calculates the orthodromic distance in meters between this position and another using the Haversine formula.
     *
     * @param other destination coordinates
     * @return distance in meters
     */
    public double distanceMetersTo(GeoCoordinates other) {
        Objects.requireNonNull(other, "Destination coordinates cannot be null");
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude());
        double deltaLat = Math.toRadians(other.latitude() - this.latitude);
        double deltaLon = Math.toRadians(other.longitude() - this.longitude);

        double a = Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return EARTH_RADIUS_METERS * c;
    }

    @Override
    public String toString() {
        return String.format("%.6f, %.6f", latitude, longitude);
    }
}
