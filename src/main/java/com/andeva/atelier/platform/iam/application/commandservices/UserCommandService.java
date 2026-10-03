package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateWithGoogleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RegisterUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResetPasswordCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.UpdateUserProfileCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.VerifyEmailTokenCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public command service interface orchestrating User authentication and account lifecycle mutations.
 *
 * @author Joel Huamani Estefanero
 */
public interface UserCommandService {

    Result<User, ApplicationError> handle(RegisterUserCommand command);

    Result<AuthenticatedUser, ApplicationError> handle(AuthenticateUserCommand command);

    Result<AuthenticatedUser, ApplicationError> handle(AuthenticateWithGoogleCommand command);

    Result<Void, ApplicationError> handle(VerifyEmailTokenCommand command);

    Result<Void, ApplicationError> handle(RequestPasswordResetCommand command);

    Result<Void, ApplicationError> handle(ResetPasswordCommand command);

    Result<User, ApplicationError> handle(UpdateUserProfileCommand command);
}
