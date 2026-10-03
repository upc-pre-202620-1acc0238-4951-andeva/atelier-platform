package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for confirming email verification using OTP token.
 *
 * @param email User account email address
 * @param token One-time numeric or alphanumeric verification token
 * @author Joel Huamani Estefanero
 */
public record VerifyEmailResource(
        @Email(message = "{iam.validation.user.email.format}")
        String email,

        @NotBlank(message = "{iam.validation.auth.token.required}")
        String token
) {
    public VerifyEmailResource(String token) {
        this(null, token);
    }
}
