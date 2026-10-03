package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request payload for inviting a prospective staff member to a tenant workshop.
 *
 * @param email  Destination email address of the invitee
 * @param roleId Unique identifier of the initial role to assign upon acceptance
 * @author Joel Huamani Estefanero
 */
public record InviteStaffResource(
        @NotBlank @Email @Size(max = 150)
        String email,

        @NotNull
        UUID roleId
) {
}
