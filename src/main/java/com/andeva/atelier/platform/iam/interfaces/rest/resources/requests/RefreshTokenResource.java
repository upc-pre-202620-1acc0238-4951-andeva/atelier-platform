package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload carrying the device's refresh token.
 *
 * @param refreshToken opaque refresh token returned at sign-in
 */
public record RefreshTokenResource(
        @NotBlank(message = "{iam.validation.auth.refresh_token.required}")
        String refreshToken
) {
}
