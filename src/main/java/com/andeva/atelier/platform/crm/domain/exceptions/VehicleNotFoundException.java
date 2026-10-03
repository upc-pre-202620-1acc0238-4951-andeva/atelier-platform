package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class VehicleNotFoundException extends CrmDomainException {

    public VehicleNotFoundException(UUID vehicleId) {
        super("VEHICLE_NOT_FOUND", String.format("Vehicle with identifier %s was not found", vehicleId));
    }

    public VehicleNotFoundException(String plate) {
        super("VEHICLE_NOT_FOUND", String.format("Vehicle with license plate %s was not found", plate));
    }
}
