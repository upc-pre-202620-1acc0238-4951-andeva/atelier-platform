package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing an OBD-II device registered in the workshop inventory.
 *
 * @author Joel Huamani Estefanero
 */
public record Obd2DeviceResponse(
        UUID id,
        UUID tenantId,
        String deviceIdentifier,
        String connectionType,
        String status,
        String hardwareModel,
        String firmwareVersion,
        Instant createdAt
) implements Serializable {
}
