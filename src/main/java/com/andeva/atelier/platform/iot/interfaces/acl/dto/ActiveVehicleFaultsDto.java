package com.andeva.atelier.platform.iot.interfaces.acl.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable DTO representing active vehicle diagnostic trouble codes (DTCs).
 *
 * @author Joel Huamani Estefanero
 */
public record ActiveVehicleFaultsDto(
        UUID faultId,
        String dtcCode,
        String severity,
        String description,
        Instant detectedAt
) implements Serializable {
}
