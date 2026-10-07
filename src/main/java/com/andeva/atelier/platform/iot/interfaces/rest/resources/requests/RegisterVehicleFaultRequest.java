package com.andeva.atelier.platform.iot.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.UUID;

/**
 * Request DTO for registering an electronic diagnostic trouble code (DTC) detected in vehicle ECUs.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterVehicleFaultRequest(
        @NotNull(message = "{error.iot.vehicle.id.required}")
        UUID vehicleId,

        @NotBlank(message = "{error.iot.fault.dtc_code.required}")
        @Pattern(regexp = "^[PBUC][0-9A-Fa-f]{4}$", message = "{error.iot.fault.invalid_dtc}")
        String dtcCode,

        @NotBlank(message = "{error.iot.fault.severity.required}")
        @Pattern(regexp = "^(MINOR|MODERATE|CRITICAL)$", message = "{error.iot.fault.severity.invalid}")
        String severity,

        String description
) implements Serializable {
}
