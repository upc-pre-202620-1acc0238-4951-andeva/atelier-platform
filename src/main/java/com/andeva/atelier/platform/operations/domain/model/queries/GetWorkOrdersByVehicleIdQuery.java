package com.andeva.atelier.platform.operations.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

public record GetWorkOrdersByVehicleIdQuery(
        VehicleId vehicleId
) {
}
