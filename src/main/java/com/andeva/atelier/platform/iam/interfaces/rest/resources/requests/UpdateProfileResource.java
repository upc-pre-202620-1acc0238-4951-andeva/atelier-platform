package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating user profile contact information and demographic details.
 *
 * @param firstName Given first name
 * @param lastName  Full surname
 * @param phone     Optional mobile contact phone number
 * @author Joel Huamani Estefanero
 */
public record UpdateProfileResource(
        @NotBlank @Size(max = 100)
        String firstName,

        @NotBlank @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^\\+?[0-9]{9,15}$")
        String phone
) {
}
