package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;

/**
 * Base domain exception for the IoT Telemetry & Predictive Maintenance Bounded Context.
 * Extends {@link DomainException} and carries standardized error codes aligned with RFC 7807 Problem Details and i18n message bundles.
 *
 * @author Joel Huamani Estefanero
 */
public class IoTDomainException extends DomainException {

    public IoTDomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    public IoTDomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message);
        if (cause != null) {
            initCause(cause);
        }
    }

    public IoTDomainException(String message) {
        super("IOT_DOMAIN_ERROR", message);
    }

    public String getErrorCode() {
        return errorCode();
    }
}
