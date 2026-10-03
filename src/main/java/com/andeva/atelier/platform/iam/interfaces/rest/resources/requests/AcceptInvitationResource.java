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
        @NotBlank(message = "{iam.validation.invitation.token.required}")
        String token,

        @NotBlank(message = "{iam.validation.user.password.required}")
        @Size(min = 8, max = 64, message = "{iam.validation.user.password.size}")
        String password,

        @NotBlank(message = "{iam.validation.user.first_name.required}")
        @Size(max = 100, message = "{iam.validation.user.first_name.size}")
        String firstName,

        @NotBlank(message = "{iam.validation.user.last_name.required}")
        @Size(max = 100, message = "{iam.validation.user.last_name.size}")
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "{iam.validation.user.phone.format}")
        String phone
) {
}
