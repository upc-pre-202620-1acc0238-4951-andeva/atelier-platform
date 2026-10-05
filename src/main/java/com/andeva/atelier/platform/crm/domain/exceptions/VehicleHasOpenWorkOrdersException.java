package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class VehicleHasOpenWorkOrdersException extends CrmDomainException {

    public VehicleHasOpenWorkOrdersException(UUID vehicleId) {
        super("VEHICLE_HAS_OPEN_WORK_ORDERS", String.format("Vehicle %s has active work orders in workshop and cannot be transferred", vehicleId));
    }
}
