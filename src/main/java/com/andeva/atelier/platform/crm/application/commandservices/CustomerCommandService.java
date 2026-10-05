package com.andeva.atelier.platform.crm.application.commandservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.commands.DeactivateCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.UpdateCustomerContactCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public application port for customer management commands.
 *
 * @author Adiel Sanchez Santin
 */
public interface CustomerCommandService {

    Result<Customer, ApplicationError> handle(RegisterIndividualCustomerCommand command);

    Result<Customer, ApplicationError> handle(RegisterCompanyCustomerCommand command);

    Result<Customer, ApplicationError> handle(UpdateCustomerContactCommand command);

    Result<Customer, ApplicationError> handle(DeactivateCustomerCommand command);
}
