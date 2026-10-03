package com.andeva.atelier.platform.iam.interfaces.rest.transform;

import com.andeva.atelier.platform.iam.domain.model.commands.AuthenticateUserCommand;
import com.andeva.atelier.platform.iam.interfaces.rest.resources.requests.SignInResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

import java.util.Objects;

/**
 * Assembler transforming {@link SignInResource} into {@link AuthenticateUserCommand}.
 *
 * @author Joel Huamani Estefanero
 */
public final class SignInCommandFromResourceAssembler {

    private SignInCommandFromResourceAssembler() {
    }

    /**
     * Converts a {@link SignInResource} into a domain {@link AuthenticateUserCommand}.
     *
     * @param resource Incoming REST sign-in payload
     * @return Initialized domain authentication command
     */
    public static AuthenticateUserCommand toCommandFromResource(SignInResource resource) {
        Objects.requireNonNull(resource, "SignInResource cannot be null");
        return new AuthenticateUserCommand(
                EmailAddress.of(resource.email()),
                resource.password()
        );
    }
}
