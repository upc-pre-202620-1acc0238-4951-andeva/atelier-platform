package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an OBD-II device is decoupled from a vehicle, terminating the monitoring session.
 *
 * @author Joel Huamani Estefanero
 */
public record DeviceUninstalledFromVehicleEvent(
        InstallationId installationId,
        VehicleId vehicleId,
        int finalOdometerKm,
        Instant timestamp
) implements Serializable {

    public DeviceUninstalledFromVehicleEvent {
        Objects.requireNonNull(installationId, "InstallationId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        if (finalOdometerKm < 0) {
            throw new IllegalArgumentException("Final odometer cannot be negative: " + finalOdometerKm);
        }
    }

    public static DeviceUninstalledFromVehicleEvent of(
            InstallationId installationId,
            VehicleId vehicleId,
            int finalOdometerKm
    ) {
        return new DeviceUninstalledFromVehicleEvent(installationId, vehicleId, finalOdometerKm, Instant.now());
    }
}
