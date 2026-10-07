package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;

/**
 * Domain exception thrown when attempting to install an OBD-II device on a vehicle
 * while the device is already actively mounted on another vehicle.
 * Maps to HTTP 409 Conflict under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class DeviceAlreadyInstalledException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_DEVICE_ALREADY_INSTALLED";

    public DeviceAlreadyInstalledException(DeviceId deviceId) {
        super(ERROR_CODE, "OBD-II device with identifier " + deviceId + " is already actively installed on another vehicle.");
    }

    public DeviceAlreadyInstalledException(String message) {
        super(ERROR_CODE, message);
    }
}
