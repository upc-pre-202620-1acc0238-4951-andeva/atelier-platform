package com.andeva.atelier.platform.iot.domain.exceptions;

import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;

/**
 * Domain exception thrown when a vehicle DTC fault record is not found.
 * Maps to HTTP 404 Not Found under RFC 7807.
 *
 * @author Joel Huamani Estefanero
 */
public class VehicleFaultNotFoundException extends IoTDomainException {

    public static final String ERROR_CODE = "ERR_VEHICLE_FAULT_NOT_FOUND";

    public VehicleFaultNotFoundException(FaultId id) {
        super(ERROR_CODE, "Vehicle fault record with identifier " + id + " was not found.");
    }

    public VehicleFaultNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
