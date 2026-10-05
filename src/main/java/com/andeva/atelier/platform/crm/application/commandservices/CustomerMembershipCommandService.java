package com.andeva.atelier.platform.crm.application.commandservices;

import com.andeva.atelier.platform.crm.domain.model.commands.InviteCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RevokeCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public application port for corporate fleet membership commands.
 *
 * @author Adiel Sanchez Santin
 */
public interface CustomerMembershipCommandService {

    Result<CustomerMembership, ApplicationError> handle(InviteCustomerMemberCommand command);

    Result<Void, ApplicationError> handle(RevokeCustomerMemberCommand command);
}
