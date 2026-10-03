package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for resetting user password with a received token.
 *
 * @param token       Password reset cryptographic token
 * @param newPassword New plaintext password meeting complexity constraints
 * @author Joel Huamani Estefanero
 */
public record ResetPasswordResource(
        @NotBlank
        String token,

        @NotBlank @Size(min = 8, max = 64)
        String newPassword
) {
}
