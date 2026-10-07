package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain exception thrown when no vehicle health diagnostic report exists for a vehicle.
 * Maps to HTTP 404 Not Found under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class VehicleHealthReportNotFoundException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_HEALTH_REPORT_NOT_FOUND";

    public VehicleHealthReportNotFoundException(VehicleId vehicleId) {
        super(ERROR_CODE, "El vehículo con ID " + vehicleId.value() + " no cuenta con reportes periciales de salud mecánica previos en el sistema.");
    }

    public VehicleHealthReportNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
