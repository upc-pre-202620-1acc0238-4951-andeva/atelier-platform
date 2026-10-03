package com.andeva.atelier.platform.crm.domain.exceptions;

import java.util.UUID;

public class VehicleActiveOwnershipNotFoundException extends CrmDomainException {

    public VehicleActiveOwnershipNotFoundException(UUID vehicleId) {
        super("VEHICLE_ACTIVE_OWNERSHIP_NOT_FOUND", String.format("Vehicle %s does not currently have an active owner registered", vehicleId));
    }
}
