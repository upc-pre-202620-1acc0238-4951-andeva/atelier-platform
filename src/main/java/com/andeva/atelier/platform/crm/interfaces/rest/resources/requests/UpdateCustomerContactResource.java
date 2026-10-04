package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Size;

/**
 * REST request for updating customer contact channels.
 *
 * @author Adiel Sanchez Santin
 */
public record UpdateCustomerContactResource(
        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @Size(max = 150, message = "Email address must not exceed 150 characters")
        String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone
) {}
