package com.andeva.atelier.platform.iot.interfaces.acl.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable DTO representing latest vehicle telemetry metrics for downstream bounded contexts.
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleLatestTelemetryDto(
        UUID vehicleId,
        Instant timestamp,
        double speedKmh,
        double engineTempCelsius,
        int engineRpm,
        Double batteryVoltage
) implements Serializable {
}
