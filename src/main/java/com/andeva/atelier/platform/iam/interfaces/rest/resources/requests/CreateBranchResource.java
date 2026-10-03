package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a physical workshop branch with GPS coordinates and geofence.
 *
 * @param name                 Descriptive branch name
 * @param sunatCode            4-digit establishment code assigned by SUNAT
 * @param latitude             WGS84 centroid latitude coordinate
 * @param longitude            WGS84 centroid longitude coordinate
 * @param geofenceRadiusMeters Geofence circular radius in meters for staff attendance
 * @author Joel Huamani Estefanero
 */
public record CreateBranchResource(
        @NotBlank @Size(max = 100)
        String name,

        @Pattern(regexp = "^\\d{4}$")
        String sunatCode,

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,

        @Min(10) @Max(1000)
        int geofenceRadiusMeters
) {
}
