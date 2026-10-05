package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST request for registering a corporate company customer.
 *
 * @author Adiel Sanchez Santin
 */
public record CreateCompanyCustomerResource(
        @NotBlank(message = "Company name is mandatory")
        @Size(max = 150, message = "Company name must not exceed 150 characters")
        String companyName,

        @NotBlank(message = "Tax ID is mandatory")
        @Size(min = 11, max = 20, message = "Company Tax ID (RUC) must be between 11 and 20 characters")
        String taxId,

        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must not exceed 150 characters")
        String email,

        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone
) {}
