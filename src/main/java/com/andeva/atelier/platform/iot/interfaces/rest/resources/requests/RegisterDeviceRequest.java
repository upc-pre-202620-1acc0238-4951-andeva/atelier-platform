package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Request DTO for registering a physical OBD-II telematics device in the workshop inventory.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterDeviceRequest(
        @NotBlank(message = "{error.iot.device.identifier.required}")
        String deviceIdentifier,

        @NotBlank(message = "{error.iot.device.connection_type.required}")
        String connectionType,

        String hardwareModel,

        String firmwareVersion
) implements Serializable {
}
