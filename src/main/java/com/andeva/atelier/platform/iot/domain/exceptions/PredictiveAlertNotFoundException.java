package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;

/**
 * Domain exception thrown when a predictive alert record is not found.
 * Maps to HTTP 404 Not Found under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class PredictiveAlertNotFoundException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_PREDICTIVE_ALERT_NOT_FOUND";

    public PredictiveAlertNotFoundException(AlertId id) {
        super(ERROR_CODE, "Predictive alert with identifier " + id + " was not found.");
    }

    public PredictiveAlertNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
