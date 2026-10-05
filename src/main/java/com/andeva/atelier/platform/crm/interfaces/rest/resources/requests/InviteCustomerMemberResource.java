package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * REST request for inviting or assigning a user to a corporate customer fleet.
 *
 * @author Adiel Sanchez Santin
 */
public record InviteCustomerMemberResource(
        @NotNull(message = "User ID is mandatory")
        UUID userId,

        @NotBlank(message = "Fleet role is mandatory")
        String role
) {}
