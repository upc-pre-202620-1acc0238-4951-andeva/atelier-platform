package com.andeva.atelier.platform.hr.domain.services;

import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.HaversineDistance;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class HaversineGeofencingService {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    public HaversineDistance calculateDistance(GeoCoordinates origin, GeoCoordinates destination) {
        Objects.requireNonNull(origin, "origin coordinates cannot be null");
        Objects.requireNonNull(destination, "destination coordinates cannot be null");

        double lat1 = Math.toRadians(origin.latitude());
        double lon1 = Math.toRadians(origin.longitude());
        double lat2 = Math.toRadians(destination.latitude());
        double lon2 = Math.toRadians(destination.longitude());

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double a = Math.sin(deltaLat / 2.0) * Math.sin(deltaLat / 2.0)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2.0) * Math.sin(deltaLon / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        double distanceMeters = EARTH_RADIUS_METERS * c;

        return HaversineDistance.of(distanceMeters);
    }

    public boolean isWithinGeofence(GeoCoordinates employeeLocation, GeoCoordinates branchCentroid, double allowedRadiusMeters) {
        if (allowedRadiusMeters < 0) {
            throw new IllegalArgumentException("Allowed radius cannot be negative");
        }
        HaversineDistance distance = calculateDistance(employeeLocation, branchCentroid);
        return distance.isWithin(allowedRadiusMeters);
    }
}
