package com.andeva.atelier.platform.iot.domain.exceptions;

/**
 * Domain exception thrown when an OBD-II hardware identifier violates the strict MAC address
 * or IMEI format requirements.
 * Maps to HTTP 422 Unprocessable Entity under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidDeviceIdentifierException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_INVALID_DEVICE_IDENTIFIER";

    public InvalidDeviceIdentifierException(String identifier) {
        super(ERROR_CODE, "The device identifier '" + identifier + "' is invalid. It must be a valid MAC address (6 hex pairs) or IMEI (15 digits).");
    }

    public InvalidDeviceIdentifierException(String identifier, String reason) {
        super(ERROR_CODE, "The device identifier '" + identifier + "' is invalid: " + reason);
    }
}
