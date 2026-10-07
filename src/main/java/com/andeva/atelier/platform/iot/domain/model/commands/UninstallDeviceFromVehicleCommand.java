package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain command to uncouple an OBD-II device from a vehicle and finalize the session.
 *
 * @author Joel Huamani Estefanero
 */
public record UninstallDeviceFromVehicleCommand(
        InstallationId installationId,
        int finalOdometerKm
) implements Serializable {

    public UninstallDeviceFromVehicleCommand {
        Objects.requireNonNull(installationId, "InstallationId cannot be null");
        if (finalOdometerKm < 0) {
            throw new IllegalArgumentException("Final odometer reading cannot be negative: " + finalOdometerKm);
        }
    }
}
