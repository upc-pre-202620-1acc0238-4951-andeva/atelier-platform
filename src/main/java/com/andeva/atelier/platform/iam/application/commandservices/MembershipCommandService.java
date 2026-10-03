package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.domain.model.aggregates.TenantMembership;
import com.andeva.atelier.platform.iam.domain.model.commands.AssignRolesToMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.DeactivateMembershipCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateMembershipCompensationCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public command service interface orchestrating staff TenantMembership operations.
 *
 * @author Joel Huamani Estefanero
 */
public interface MembershipCommandService {

    Result<TenantMembership, ApplicationError> handle(AssignRolesToMembershipCommand command);

    Result<TenantMembership, ApplicationError> handle(UpdateMembershipCompensationCommand command);

    Result<Void, ApplicationError> handle(DeactivateMembershipCommand command);
}
