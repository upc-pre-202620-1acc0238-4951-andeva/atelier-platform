package com.andeva.atelier.platform.iot.application.commandservices;

import com.andeva.atelier.platform.iot.domain.model.commands.RegisterObd2DeviceCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UpdateDeviceStatusCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.DeviceId;

/**
 * Command Service for registering OBD-II hardware scanners and managing their operational status.
 *
 * @author Joel Huamani Estefanero
 */
public interface Obd2DeviceCommandService {

    DeviceId handle(RegisterObd2DeviceCommand command);

    void handle(UpdateDeviceStatusCommand command);
}
