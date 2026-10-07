package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command to couple an OBD-II device to a vehicle and initiate telemetry tracking.
 *
 * @author Joel Huamani Estefanero
 */
public record InstallDeviceOnVehicleCommand(
        DeviceId deviceId,
        VehicleId vehicleId,
        TenantId tenantId,
        int currentOdometerKm
) implements Serializable {

    public InstallDeviceOnVehicleCommand {
        Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        if (currentOdometerKm < 0) {
            throw new IllegalArgumentException("Current odometer cannot be negative: " + currentOdometerKm);
        }
    }
}
