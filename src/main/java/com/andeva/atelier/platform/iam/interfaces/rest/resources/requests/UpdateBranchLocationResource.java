package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating the GPS coordinates and geofence radius of a branch.
 *
 * @param latitude             Updated WGS84 latitude coordinate
 * @param longitude            Updated WGS84 longitude coordinate
 * @param geofenceRadiusMeters Updated geofence circular radius in meters
 * @author Joel Huamani Estefanero
 */
public record UpdateBranchLocationResource(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,

        @Min(10) @Max(1000)
        int geofenceRadiusMeters
) {
}
