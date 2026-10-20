package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain exception thrown when a vehicle has neither telemetry nor DTC faults in the requested
 * window, so a predictive diagnosis would be unreliable.
 * Maps to HTTP 422 Unprocessable Entity under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class InsufficientTelemetryException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_INSUFFICIENT_TELEMETRY";

    public InsufficientTelemetryException(VehicleId vehicleId, int daysToAnalyze) {
        super(ERROR_CODE, "El vehículo con ID " + vehicleId.value() + " no registra telemetría ni códigos de falla en los últimos "
                + daysToAnalyze + " días: el volumen de datos es insuficiente para un diagnóstico predictivo confiable.");
    }
}
