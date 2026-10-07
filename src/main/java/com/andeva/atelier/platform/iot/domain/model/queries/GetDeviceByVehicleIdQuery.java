package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain query to look up the currently active OBD-II device coupled to a vehicle.
 *
 * @author Joel Huamani Estefanero
 */
public record GetDeviceByVehicleIdQuery(
        VehicleId vehicleId
) implements Serializable {

    public GetDeviceByVehicleIdQuery {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
    }
}
