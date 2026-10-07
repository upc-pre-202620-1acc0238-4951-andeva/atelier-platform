package com.andeva.atelier.platform.iot.domain.model.commands;

import com.andeva.atelier.platform.iot.domain.model.enums.DeviceStatus;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;

import java.util.Objects;

/**
 * Command to update the operational status of an OBD-II device.
 *
 * @param deviceId  the unique device identifier
 * @param newStatus the target operational status
 * @author Joel Huamani Estefanero
 */
public record UpdateDeviceStatusCommand(
        DeviceId deviceId,
        DeviceStatus newStatus
) {
    public UpdateDeviceStatusCommand {
        Objects.requireNonNull(deviceId, "DeviceId cannot be null");
        Objects.requireNonNull(newStatus, "DeviceStatus cannot be null");
    }
}
