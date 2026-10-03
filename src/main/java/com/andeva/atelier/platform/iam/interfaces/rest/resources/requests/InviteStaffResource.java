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
        @NotBlank(message = "{iam.validation.user.email.required}")
        @Email(message = "{iam.validation.user.email.format}")
        @Size(max = 150, message = "{iam.validation.user.email.size}")
        String email,

        @NotNull(message = "{iam.validation.role.id.required}")
        UUID roleId
) {
}
