package com.andeva.atelier.platform.crm.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Affiliation of a person who already has a platform account.
 *
 * @param email   email of the platform account
 * @param taxId   identity document (DNI) required for the workshop's customer record
 * @param phone   optional contact phone
 */
public record AffiliateCustomerResource(
        @NotBlank @Email String email,
        @NotBlank String taxId,
        String phone
) {
}
