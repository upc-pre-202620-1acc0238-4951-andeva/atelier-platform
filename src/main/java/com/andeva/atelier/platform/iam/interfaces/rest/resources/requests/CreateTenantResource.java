package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating and provisioning a new workshop tenant and its initial admin account.
 *
 * @param name           Commercial or brand name of the workshop
 * @param legalName      Official legal entity name registered before SUNAT
 * @param taxId          Valid 11-digit Peruvian RUC starting with 10, 15, 17, or 20
 * @param adminEmail     Corporate email of the workshop owner/administrator
 * @param adminPassword  Plaintext password for the administrator account
 * @param adminFirstName Given first name of the administrator
 * @param adminLastName  Full surname of the administrator
 * @param adminPhone     Mobile contact telephone number
 * @author Joel Huamani Estefanero
 */
public record CreateTenantResource(
        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Size(max = 150)
        String legalName,

        @NotBlank @Pattern(regexp = "^(10|15|17|20)\\d{9}$")
        String taxId,

        @NotBlank @Email @Size(max = 150)
        String adminEmail,

        @NotBlank @Size(min = 8, max = 64)
        String adminPassword,

        @NotBlank @Size(max = 100)
        String adminFirstName,

        @NotBlank @Size(max = 100)
        String adminLastName,

        @NotBlank @Pattern(regexp = "^\\+?[0-9]{9,15}$")
        String adminPhone
) {
}
