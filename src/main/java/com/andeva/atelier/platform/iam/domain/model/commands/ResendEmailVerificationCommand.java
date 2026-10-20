package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

import java.util.Objects;

/**
 * Domain command to issue a fresh email verification code for an account still pending verification.
 *
 * @author Joel Huamani Estefanero
 */
public record ResendEmailVerificationCommand(
        EmailAddress email
) {
    public ResendEmailVerificationCommand {
        Objects.requireNonNull(email, "Email address cannot be null");
    }
}
