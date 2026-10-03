package com.andeva.atelier.platform.iam.domain.model.commands;

import com.andeva.atelier.platform.iam.domain.model.valueobjects.Password;

import java.util.Objects;

/**
 * Domain command to complete password reset using a token and new password.
 *
 * @author Joel Huamani Estefanero
 */
public record ResetPasswordCommand(
        String token,
        Password newPassword
) {
    public ResetPasswordCommand {
        Objects.requireNonNull(token, "Reset token cannot be null");
        Objects.requireNonNull(newPassword, "New password cannot be null");
        if (token.trim().isEmpty()) {
            throw new IllegalArgumentException("Reset token cannot be empty");
        }
    }
}
