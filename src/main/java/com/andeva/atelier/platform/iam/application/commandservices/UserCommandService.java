package com.andeva.atelier.platform.iam.application.commandservices;

import com.andeva.atelier.platform.iam.application.internal.dto.AuthenticatedUser;
import com.andeva.atelier.platform.iam.domain.model.aggregates.User;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateWithGoogleCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RefreshAccessTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RegisterDeviceTokenCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.SignOutCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RegisterUserCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.RequestPasswordResetCommand;
import com.andeva.atelier.platform.iam.domain.model.commands.ResendEmailVerificationCommand;
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

    /**
     * Exchanges a valid refresh token for a new access token (with current permissions) and a rotated refresh token.
     */
    Result<AuthenticatedUser, ApplicationError> handle(RefreshAccessTokenCommand command);

    /**
     * Revokes the refresh token of the device; the short-lived access token simply expires.
     */
    Result<Void, ApplicationError> handle(SignOutCommand command);

    Result<Void, ApplicationError> handle(VerifyEmailTokenCommand command);

    Result<Void, ApplicationError> handle(ResendEmailVerificationCommand command);

    Result<Void, ApplicationError> handle(RequestPasswordResetCommand command);

    Result<Void, ApplicationError> handle(ResetPasswordCommand command);

    Result<User, ApplicationError> handle(UpdateUserProfileCommand command);

    Result<Void, ApplicationError> handle(RegisterDeviceTokenCommand command);
}
