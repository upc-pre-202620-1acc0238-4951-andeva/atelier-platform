package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

import java.util.Objects;

/**
 * Domain command to request a password reset procedure for an email address.
 *
 * @author Joel Huamani Estefanero
 */
public record RequestPasswordResetCommand(
        EmailAddress email
) {
    public RequestPasswordResetCommand {
        Objects.requireNonNull(email, "Email address cannot be null");
    }
}
