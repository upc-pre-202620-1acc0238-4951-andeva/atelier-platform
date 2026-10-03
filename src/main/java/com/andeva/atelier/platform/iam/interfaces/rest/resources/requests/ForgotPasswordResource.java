package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for requesting a password reset email token.
 *
 * @param email Registered user email address
 * @author Joel Huamani Estefanero
 */
public record ForgotPasswordResource(
        @NotBlank(message = "{iam.validation.user.email.required}")
        @Email(message = "{iam.validation.user.email.format}")
        String email
) {
}
