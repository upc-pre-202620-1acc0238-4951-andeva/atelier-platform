package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO representing a batch of chronological telemetry readings emitted by an OBD-II scanner.
 *
 * @author Joel Huamani Estefanero
 */
public record TelemetryBatchRequest(
        @NotNull(message = "{error.iot.vehicle.id.required}")
        UUID vehicleId,

        @NotEmpty(message = "{error.iot.telemetry.readings.empty}")
        @Valid
        List<TelemetryReadingItemDto> readings
) implements Serializable {
}
