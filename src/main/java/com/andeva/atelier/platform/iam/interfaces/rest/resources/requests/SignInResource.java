package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for local credentials sign-in authentication.
 *
 * @param email    Registered user email
 * @param password Plaintext password for authentication
 * @author Joel Huamani Estefanero
 */
public record SignInResource(
        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {
}
