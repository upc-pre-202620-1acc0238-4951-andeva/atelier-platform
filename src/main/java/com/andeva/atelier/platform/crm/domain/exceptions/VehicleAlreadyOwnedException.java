package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class VehicleAlreadyOwnedException extends CrmDomainException {
    public VehicleAlreadyOwnedException(UUID vehicleId, UUID customerId) {
        super("VEHICLE_ALREADY_OWNED", String.format("El vehiculo %s ya se encuentra registrado bajo la titularidad del cliente %s", vehicleId, customerId));
    }
}
