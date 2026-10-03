package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;

import java.util.Objects;

/**
 * Domain command to validate an email address through an OTP verification token.
 *
 * @author Joel Huamani Estefanero
 */
public record VerifyEmailTokenCommand(
        EmailAddress email,
        String token
) {
    public VerifyEmailTokenCommand {
        Objects.requireNonNull(token, "Verification token cannot be null");
        if (token.trim().isEmpty()) {
            throw new IllegalArgumentException("Verification token cannot be empty");
        }
    }

    public VerifyEmailTokenCommand(String token) {
        this(null, token);
    }
}
