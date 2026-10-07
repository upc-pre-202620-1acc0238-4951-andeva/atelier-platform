package com.andeva.atelier.platform.iot.application.commandservices;

import com.andeva.atelier.platform.iot.domain.model.commands.InstallDeviceOnVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.UninstallDeviceFromVehicleCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.InstallationId;

/**
 * Command Service for managing physical installation and uninstallation of OBD-II devices on vehicles.
 *
 * @author Joel Huamani Estefanero
 */
public interface DeviceInstallationCommandService {

    InstallationId handle(InstallDeviceOnVehicleCommand command);

    void handle(UninstallDeviceFromVehicleCommand command);
}
