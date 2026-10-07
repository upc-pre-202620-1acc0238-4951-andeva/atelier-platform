package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing an electronic diagnostic trouble code (DTC) logged for a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleFaultResponse(
        UUID id,
        UUID vehicleId,
        String dtcCode,
        String severity,
        String description,
        Instant detectedAt,
        boolean isResolved,
        Instant resolvedAt
) implements Serializable {
}
