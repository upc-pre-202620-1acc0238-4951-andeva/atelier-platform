package com.andeva.atelier.platform.iam.interfaces.acl.dto;

import java.io.Serializable;
import java.util.UUID;

/**
 * Inbound Anti-Corruption Layer DTO representing branch geographic coordinates and geofence radius.
 *
 * @author Joel Huamani Estefanero
 */
public record BranchGeofenceAclDto(
        UUID branchId,
        double latitude,
        double longitude,
        int radiusMeters
) implements Serializable {
}
