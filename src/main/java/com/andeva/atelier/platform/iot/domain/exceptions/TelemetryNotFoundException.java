package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain exception thrown when no telemetry records exist for a specified vehicle.
 * Maps to HTTP 404 Not Found under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class TelemetryNotFoundException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_TELEMETRY_NOT_FOUND";

    public TelemetryNotFoundException(VehicleId vehicleId) {
        super(ERROR_CODE, "El vehículo con ID " + vehicleId.value() + " no registra ninguna lectura telemétrica en la base de datos.");
    }

    public TelemetryNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
