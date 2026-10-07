package com.andeva.atelier.platform.iot.domain.model.queries;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain query to retrieve the most recent instantaneous telemetry reading for real-time tachometer display.
 *
 * @author Joel Huamani Estefanero
 */
public record GetVehicleLatestTelemetryQuery(
        VehicleId vehicleId
) implements Serializable {

    public GetVehicleLatestTelemetryQuery {
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
    }
}
