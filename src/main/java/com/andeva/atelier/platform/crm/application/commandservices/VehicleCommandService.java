package com.andeva.atelier.platform.crm.application.commandservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.TransferVehicleOwnershipCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public application port for vehicle lifecycle and ownership commands.
 *
 * @author Adiel Sanchez Santin
 */
public interface VehicleCommandService {

    Result<Vehicle, ApplicationError> handle(RegisterVehicleCommand command);

    Result<Vehicle, ApplicationError> handle(TransferVehicleOwnershipCommand command);

    Result<Vehicle, ApplicationError> handle(com.andeva.atelier.platform.crm.domain.model.commands.UpdateVehicleCommand command);

    Result<Vehicle, ApplicationError> handle(com.andeva.atelier.platform.crm.domain.model.commands.UpdateMileageCommand command);
}
