package com.andeva.atelier.platform.iot.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response representing the most recent telemetry reading captured for a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record VehicleLatestTelemetryResponse(
        UUID vehicleId,
        Instant timestamp,
        Double latitude,
        Double longitude,
        int speedKmh,
        double engineTempCelsius,
        int engineRpm,
        Double batteryVoltage,
        Double fuelPercentage
) implements Serializable {
}
