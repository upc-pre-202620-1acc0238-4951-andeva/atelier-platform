package com.andeva.atelier.platform.crm.domain.exceptions;

public class VehicleAlreadyExistsException extends CrmDomainException {

    public VehicleAlreadyExistsException(String plate) {
        super("VEHICLE_ALREADY_EXISTS", String.format("A vehicle with license plate %s already exists in the global registry", plate));
    }
}
