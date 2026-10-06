package com.andeva.atelier.platform.inventory.application.commandservices;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.Supplier;
import com.andeva.atelier.platform.inventory.domain.model.commands.DeactivateSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.RegisterSupplierCommand;
import com.andeva.atelier.platform.inventory.domain.model.commands.UpdateSupplierCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface SupplierCommandService {

    Result<Supplier, ApplicationError> handle(RegisterSupplierCommand command);

    Result<Supplier, ApplicationError> handle(UpdateSupplierCommand command);

    Result<Void, ApplicationError> handle(DeactivateSupplierCommand command);
}
