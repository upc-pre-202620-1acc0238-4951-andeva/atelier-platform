package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.domain.model.aggregates.Invitation;
import com.andeva.atelier.platform.iam.domain.model.commands.AcceptInvitationCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.InviteStaffCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public command service interface orchestrating staff onboarding Invitations.
 *
 * @author Joel Huamani Estefanero
 */
public interface InvitationCommandService {

    Result<Invitation, ApplicationError> handle(InviteStaffCommand command);

    Result<AuthenticatedUser, ApplicationError> handle(AcceptInvitationCommand command);
}
