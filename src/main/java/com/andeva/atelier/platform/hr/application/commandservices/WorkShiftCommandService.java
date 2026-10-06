package com.andeva.atelier.platform.hr.application.commandservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.ActivateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.CreateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.DeactivateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateWorkShiftCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface WorkShiftCommandService {
    Result<WorkShift, ApplicationError> handle(CreateWorkShiftCommand command);
    Result<WorkShift, ApplicationError> handle(UpdateWorkShiftCommand command);
    Result<Void, ApplicationError> handle(ActivateWorkShiftCommand command);
    Result<Void, ApplicationError> handle(DeactivateWorkShiftCommand command);
}
