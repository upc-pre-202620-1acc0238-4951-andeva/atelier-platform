package com.andeva.atelier.platform.iot.application.commandservices;

import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;

/**
 * Command Service for orchestrating AI-assisted vehicle health diagnostic evaluations.
 *
 * @author Joel Huamani Estefanero
 */
public interface VehicleHealthReportCommandService {

    VehicleHealthReportAiDto handle(GenerateVehicleHealthReportCommand command);
}
