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
        @NotBlank(message = "{iam.validation.branch.name.required}")
        @Size(max = 100, message = "{iam.validation.branch.name.size}")
        String name,

        @Pattern(regexp = "^\\d{4}$", message = "{iam.validation.branch.sunat_code.format}")
        String sunatCode,

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
