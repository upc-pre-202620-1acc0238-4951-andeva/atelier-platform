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
        @NotNull(message = "{iam.validation.branch.latitude.required}")
        @DecimalMin(value = "-90.0", message = "{iam.validation.branch.latitude.range}")
        @DecimalMax(value = "90.0", message = "{iam.validation.branch.latitude.range}")
        Double latitude,

        @NotNull(message = "{iam.validation.branch.longitude.required}")
        @DecimalMin(value = "-180.0", message = "{iam.validation.branch.longitude.range}")
        @DecimalMax(value = "180.0", message = "{iam.validation.branch.longitude.range}")
        Double longitude,

        @Min(value = 10, message = "{iam.validation.branch.geofence.min}")
        @Max(value = 1000, message = "{iam.validation.branch.geofence.max}")
        int geofenceRadiusMeters
) {
}
