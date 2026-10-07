package com.andeva.atelier.platform.iot.domain.exceptions;

/**
 * Domain exception thrown when a Diagnostic Trouble Code (DTC) does not comply with
 * the SAE J2012 or ISO 15031-6 standard format (e.g., P0300, P0420, C0001, B0001, U0100).
 * Maps to HTTP 422 Unprocessable Entity under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidDtcCodeException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_INVALID_DTC_CODE";

    public InvalidDtcCodeException(String dtcCode) {
        super(ERROR_CODE, "The DTC code '" + dtcCode + "' is invalid. It must comply with SAE J2012 format (e.g. P0300, P0420).");
    }

    public InvalidDtcCodeException(String dtcCode, String reason) {
        super(ERROR_CODE, "The DTC code '" + dtcCode + "' is invalid: " + reason);
    }
}
