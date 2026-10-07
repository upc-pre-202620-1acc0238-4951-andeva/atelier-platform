package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

/**
 * Domain exception thrown when a device installation session is not found.
 * Maps to HTTP 404 Not Found under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class InstallationNotFoundException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_INSTALLATION_NOT_FOUND";

    public InstallationNotFoundException(InstallationId id) {
        super(ERROR_CODE, "Device installation session with identifier " + id + " was not found.");
    }

    public InstallationNotFoundException(VehicleId vehicleId) {
        super(ERROR_CODE, "No active device installation session was found for vehicle " + vehicleId + ".");
    }

    public InstallationNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
