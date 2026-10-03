package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for accepting a staff invitation and registering user credentials.
 *
 * @param token     Cryptographic invitation token received via email
 * @param password  Plaintext password chosen by the collaborator
 * @param firstName Given first name
 * @param lastName  Full surname
 * @param phone     Optional mobile contact phone number
 * @author Joel Huamani Estefanero
 */
public record AcceptInvitationResource(
        @NotBlank
        String token,

        @NotBlank @Size(min = 8, max = 64)
        String password,

        @NotBlank @Size(max = 100)
        String firstName,

        @NotBlank @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$")
        String phone
) {
}
