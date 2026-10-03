package com.andeva.atelier.platform.iam.interfaces.rest.resources.responses;

import java.util.UUID;

/**
 * Response projection representing a physical workshop branch establishment.
 *
 * @param id                   Universal unique identifier of the branch
 * @param tenantId             Tenant owner of the branch
 * @param name                 Descriptive name of the branch
 * @param sunatCode            4-digit establishment code assigned by SUNAT
 * @param latitude             WGS84 centroid latitude coordinate
 * @param longitude            WGS84 centroid longitude coordinate
 * @param geofenceRadiusMeters Geofence circular radius in meters
 * @param isActive             Operational status flag of the branch
 * @author Joel Huamani Estefanero
 */
public record BranchResource(
        UUID id,
        UUID tenantId,
        String name,
        String sunatCode,
        Double latitude,
        Double longitude,
        int geofenceRadiusMeters,
        boolean isActive
) {
}
