package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for federated Google OAuth2 Identity sign-in.
 *
 * @param idToken OpenID Connect Google ID token string
 * @author Joel Huamani Estefanero
 */
public record GoogleSignInResource(
        @NotBlank
        String idToken
) {
}
