package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;

/**
 * Individual kinematic, thermodynamic, or electrical telemetry sample item within an ingestion batch.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryReadingItemDto(
        @NotNull(message = "{error.iot.telemetry.timestamp.required}")
        Instant timestamp,

        Double latitude,

        Double longitude,

        @Min(value = 0, message = "{error.iot.telemetry.speed.negative}")
        int speedKmh,

        @NotNull(message = "{error.iot.telemetry.temperature.required}")
        Double engineTempCelsius,

        @Min(value = 0, message = "{error.iot.telemetry.rpm.negative}")
        int engineRpm,

        Double fuelPercentage,

        Double batteryVoltage
) implements Serializable {
}
