package com.andeva.atelier.platform.iot.domain.exceptions;

/**
 * Domain exception thrown when a batch of sensory telemetry records fails validation,
 * contains unphysical values, or encounters persistence errors in TimescaleDB.
 * Maps to HTTP 422 Unprocessable Entity under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class TimescaleIngestionException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_TIMESCALE_INGESTION_FAILED";

    public TimescaleIngestionException(String message) {
        super(ERROR_CODE, message);
    }

    public TimescaleIngestionException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
    }
}
