package com.andeva.atelier.platform.hr.domain.exceptions;

public class GeofenceViolationException extends HrDomainException {

    public GeofenceViolationException(double distanceMeters, double allowedRadiusMeters) {
        super("ERR_HR_GEOFENCE_BREACH", String.format(
                "Marcación presencial rechazada: la distancia al taller (%.2f m) excede el radio autorizado (%.2f m)",
                distanceMeters, allowedRadiusMeters));
    }

    public GeofenceViolationException(String message) {
        super("ERR_HR_GEOFENCE_BREACH", message);
    }
}
