package com.andeva.atelier.platform.hr.domain.model.valueobjects;

import java.io.Serializable;

public record GeoCoordinates(double latitude, double longitude) implements Serializable {

    public GeoCoordinates {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90.0 and 90.0 degrees: " + latitude);
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180.0 and 180.0 degrees: " + longitude);
        }
    }

    public static GeoCoordinates of(double latitude, double longitude) {
        return new GeoCoordinates(latitude, longitude);
    }
}
