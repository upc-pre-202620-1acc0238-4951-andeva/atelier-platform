package com.andeva.atelier.platform.iam.application.internal.dto;

import java.io.Serializable;
import java.util.Objects;

/**
 * Normalized user payload verified from an external Google Identity ID token.
 *
 * @author Joel Huamani Estefanero
 */
public record GoogleUserPayload(
        String googleId,
        String email,
        String givenName,
        String familyName,
        String pictureUrl,
        boolean emailVerified
) implements Serializable {

    public GoogleUserPayload {
        Objects.requireNonNull(googleId, "Google ID cannot be null");
        Objects.requireNonNull(email, "Email address cannot be null");
    }
}
