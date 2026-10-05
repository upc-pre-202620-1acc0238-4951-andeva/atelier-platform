package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkBay;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateWorkBayCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateWorkBayStatusCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface WorkBayCommandService {

    Result<WorkBay, ApplicationError> handle(CreateWorkBayCommand command);

    Result<WorkBay, ApplicationError> handle(UpdateWorkBayStatusCommand command);
}
