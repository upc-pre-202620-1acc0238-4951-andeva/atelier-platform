package com.andeva.atelier.platform.iot.domain.model.events;

import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an OBD-II device is physically coupled to a vehicle's diagnostic port.
 *
 * @author Joel Huamani Estefanero
 */
public record DeviceInstalledOnVehicleEvent(
        InstallationId installationId,
        DeviceId deviceId,
        VehicleId vehicleId,
        TenantId tenantId,
        Instant timestamp
) implements Serializable {

    public DeviceInstalledOnVehicleEvent {
        Objects.requireNonNull(installationId, "InstallationId cannot be null");
        Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        Objects.requireNonNull(vehicleId, "VehicleId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
    }

    public static DeviceInstalledOnVehicleEvent of(
            InstallationId installationId,
            DeviceId deviceId,
            VehicleId vehicleId,
            TenantId tenantId
    ) {
        return new DeviceInstalledOnVehicleEvent(installationId, deviceId, vehicleId, tenantId, Instant.now());
    }
}
