package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * Request DTO for updating the operational or administrative status of an OBD-II device.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateDeviceStatusRequest(
        @NotBlank(message = "{error.iot.device.status.required}")
        String status
) implements Serializable {
}
