package com.andeva.atelier.platform.operations.application.commandservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.Service;
import com.andeva.atelier.platform.operations.domain.model.commands.CreateServiceItemCommand;
import com.andeva.atelier.platform.operations.domain.model.commands.UpdateServiceItemCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface ServiceCommandService {

    Result<Service, ApplicationError> handle(CreateServiceItemCommand command);

    Result<Service, ApplicationError> handle(UpdateServiceItemCommand command);
}
