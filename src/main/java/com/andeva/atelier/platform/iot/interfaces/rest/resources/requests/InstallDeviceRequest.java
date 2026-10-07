package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * Request DTO for binding an available OBD-II telematics device to a workshop vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record InstallDeviceRequest(
        @NotNull(message = "{error.iot.device.id.required}")
        UUID deviceId,

        @NotNull(message = "{error.iot.vehicle.id.required}")
        UUID vehicleId,

        @Min(value = 0, message = "{error.iot.installation.odometer.negative}")
        int currentOdometerKm
) implements Serializable {
}
