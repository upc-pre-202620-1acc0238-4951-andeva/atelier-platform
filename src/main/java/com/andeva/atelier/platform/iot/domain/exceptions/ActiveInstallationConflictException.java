package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain exception thrown when attempting to install an OBD-II device on a vehicle
 * that already maintains an active hardware monitoring session.
 * Maps to HTTP 409 Conflict under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class ActiveInstallationConflictException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_ACTIVE_INSTALLATION_CONFLICT";

    public ActiveInstallationConflictException(VehicleId vehicleId) {
        super(ERROR_CODE, "Vehicle with identifier " + vehicleId + " already has an active OBD-II device installation.");
    }

    public ActiveInstallationConflictException(String message) {
        super(ERROR_CODE, message);
    }
}
