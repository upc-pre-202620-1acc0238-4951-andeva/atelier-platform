package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing an active or completed OBD-II device installation session on a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record DeviceInstallationResponse(
        UUID id,
        UUID deviceId,
        UUID vehicleId,
        UUID tenantId,
        Instant installedAt,
        Instant uninstalledAt,
        int initialOdometerKm,
        Integer finalOdometerKm,
        boolean isActive
) implements Serializable {
}
