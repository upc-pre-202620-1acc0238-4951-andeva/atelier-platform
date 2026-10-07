package com.andeva.atelier.platform.iot.application.commandservices;

import com.andeva.atelier.platform.iot.domain.model.commands.RegisterVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ResolveVehicleFaultCommand;
import com.andeva.atelier.platform.iot.domain.model.ids.FaultId;

/**
 * Command Service for logging vehicle DTC faults and recording technical resolutions.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleFaultCommandService {

    FaultId handle(RegisterVehicleFaultCommand command);

    void handle(ResolveVehicleFaultCommand command);
}
