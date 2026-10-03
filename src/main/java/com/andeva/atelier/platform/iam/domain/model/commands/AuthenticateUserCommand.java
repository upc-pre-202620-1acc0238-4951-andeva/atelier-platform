package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

import java.util.Objects;

/**
 * Domain command to authenticate a user with local email and password credentials.
 *
 * @author Joel Huamani Estefanero
 */
public record AuthenticateUserCommand(
        EmailAddress email,
        String rawPassword
) {
    public AuthenticateUserCommand {
        Objects.requireNonNull(email, "Email address cannot be null");
        Objects.requireNonNull(rawPassword, "Password cannot be null");
    }
}
