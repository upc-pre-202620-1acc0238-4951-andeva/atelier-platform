package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Min;

import java.io.Serializable;
import java.time.Instant;

/**
 * Request DTO for recording the uninstallation and final odometer reading of an OBD-II device.
 *
 * @author Joel Huamani Estefanero
 */
public record UninstallDeviceRequest(
        @Min(value = 0, message = "{error.iot.installation.odometer.negative}")
        int finalOdometerKm,

        Instant uninstalledAt
) implements Serializable {
}
