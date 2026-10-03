package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.GeoPoint;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

/**
 * Domain command to add a physical branch to an automotive workshop Tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record CreateBranchCommand(
        TenantId tenantId,
        String name,
        String sunatCode,
        GeoPoint location,
        int geofenceRadiusMeters
) {
    public CreateBranchCommand {
        Objects.requireNonNull(tenantId, "Tenant identifier cannot be null");
        Objects.requireNonNull(name, "Branch name cannot be null");
        Objects.requireNonNull(sunatCode, "SUNAT code cannot be null");
        Objects.requireNonNull(location, "Location cannot be null");
        if (geofenceRadiusMeters <= 0) {
            throw new IllegalArgumentException("Geofence radius must be positive");
        }
    }
}
