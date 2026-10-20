package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for re-sending the email verification code of a pending account.
 *
 * @param email Email address of the account pending verification
 * @author Joel Huamani Estefanero
 */
public record ResendVerificationResource(
        @NotBlank(message = "{iam.validation.user.email.required}")
        @Email(message = "{iam.validation.user.email.format}")
        String email
) {
}
