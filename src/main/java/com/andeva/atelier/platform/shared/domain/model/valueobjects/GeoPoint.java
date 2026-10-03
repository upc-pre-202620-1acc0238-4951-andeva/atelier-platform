package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import java.util.Objects;

/**
 * Geographic coordinate expressed in the WGS84 ellipsoid.
 * Encapsulates spherical orthodromic distance computation using the Haversine formula.
 *
 * @author Joel Huamani Estefanero
 */
public record GeoPoint(double latitude, double longitude) {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    public GeoPoint {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude out of WGS84 bounds [-90.0, 90.0]: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude out of WGS84 bounds [-180.0, 180.0]: " + longitude);
        }
    }

    public static GeoPoint of(double lat, double lon) {
        return new GeoPoint(lat, lon);
    }

    /**
     * Calculates the orthodromic spherical distance between two geographic points using the Haversine formula:
     * d = 2 * R * arcsin(sqrt(sin^2(delta_lat / 2) + cos(lat1) * cos(lat2) * sin^2(delta_lon / 2)))
     *
     * @param other Target geographic coordinate
     * @return Separation in meters represented by DistanceMeters
     */
    public DistanceMeters distanceTo(GeoPoint other) {
        Objects.requireNonNull(other, "Destination geographic point cannot be null");

        double phi1 = Math.toRadians(this.latitude);
        double phi2 = Math.toRadians(other.latitude);
        double deltaPhi = Math.toRadians(other.latitude - this.latitude);
        double deltaLambda = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(deltaPhi / 2.0) * Math.sin(deltaPhi / 2.0)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2.0) * Math.sin(deltaLambda / 2.0);

        double clampedA = Math.max(0.0, Math.min(1.0, a));
        double c = 2.0 * Math.atan2(Math.sqrt(clampedA), Math.sqrt(1.0 - clampedA));
        return DistanceMeters.of(EARTH_RADIUS_METERS * c);
    }
}
