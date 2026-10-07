package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.DeviceIdentifier;

/**
 * Domain exception thrown when an OBD-II device is not found by ID or hardware identifier.
 * Maps to HTTP 404 Not Found under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class DeviceNotFoundException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_DEVICE_NOT_FOUND";

    public DeviceNotFoundException(DeviceId id) {
        super(ERROR_CODE, "OBD-II device with identifier " + id + " was not found.");
    }

    public DeviceNotFoundException(DeviceIdentifier identifier) {
        super(ERROR_CODE, "OBD-II device with hardware identifier " + identifier + " was not found.");
    }

    public DeviceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
