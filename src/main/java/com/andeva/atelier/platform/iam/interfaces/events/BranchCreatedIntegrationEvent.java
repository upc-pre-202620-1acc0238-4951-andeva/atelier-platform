package com.andeva.atelier.platform.iam.interfaces.events;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a new physical workshop branch is created.
 *
 * @param branchId             Universal unique identifier of the branch
 * @param tenantId             Tenant owner of the branch
 * @param name                 Descriptive name of the branch location
 * @param sunatCode            4-digit SUNAT annex establishment code
 * @param latitude             WGS84 latitude coordinate
 * @param longitude            WGS84 longitude coordinate
 * @param geofenceRadiusMeters Geofence circular radius in meters
 * @param occurredOn           Timestamp of event occurrence
 * @author Joel Huamani Estefanero
 */
public record BranchCreatedIntegrationEvent(
        UUID branchId,
        UUID tenantId,
        String name,
        String sunatCode,
        Double latitude,
        Double longitude,
        int geofenceRadiusMeters,
        Instant occurredOn
) {
    public BranchCreatedIntegrationEvent {
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(name, "name cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }
}
