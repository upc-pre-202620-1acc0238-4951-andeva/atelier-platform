package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request for registering an individual (natural person) customer.
 *
 * @author Adiel Sanchez Santin
 */
public record CreateIndividualCustomerResource(
        @NotBlank(message = "First name is mandatory")
        @Size(max = 100, message = "First name must not exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is mandatory")
        @Size(max = 100, message = "Last name must not exceed 100 characters")
        String lastName,

        @NotBlank(message = "Tax ID is mandatory")
        @Size(min = 8, max = 20, message = "Tax ID must be between 8 and 20 characters")
        String taxId,

        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone
) {}
