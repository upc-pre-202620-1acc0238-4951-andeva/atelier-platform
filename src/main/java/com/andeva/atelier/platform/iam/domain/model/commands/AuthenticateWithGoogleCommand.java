package com.andeva.atelier.platform.iam.domain.model.commands;

import java.util.Objects;

/**
 * Domain command to authenticate a user federated via Google OAuth2 ID Token.
 *
 * @author Joel Huamani Estefanero
 */
public record AuthenticateWithGoogleCommand(
        String idToken
) {
    public AuthenticateWithGoogleCommand {
        Objects.requireNonNull(idToken, "Google ID token cannot be null");
        if (idToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Google ID token cannot be empty");
        }
    }
}
