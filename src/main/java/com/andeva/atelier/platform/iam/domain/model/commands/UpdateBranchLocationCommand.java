package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;

import java.util.Objects;

/**
 * Domain command to update a branch's GPS coordinates, geofence radius, and metadata.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateBranchLocationCommand(
        BranchId branchId,
        String name,
        String sunatCode,
        GeoPoint location,
        int geofenceRadiusMeters
) {
    public UpdateBranchLocationCommand {
        Objects.requireNonNull(branchId, "Branch identifier cannot be null");
        Objects.requireNonNull(name, "Branch name cannot be null");
        Objects.requireNonNull(sunatCode, "SUNAT code cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");
        if (geofenceRadiusMeters <= 0) {
            throw new IllegalArgumentException("Geofence radius must be positive");
        }
    }
}
