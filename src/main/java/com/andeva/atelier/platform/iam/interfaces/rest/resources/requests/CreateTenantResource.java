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
        @NotBlank(message = "{iam.validation.tenant.business_name.required}")
        @Size(max = 100, message = "{iam.validation.tenant.business_name.size}")
        String name,

        @NotBlank(message = "{iam.validation.tenant.trade_name.required}")
        @Size(max = 150, message = "{iam.validation.tenant.trade_name.size}")
        String legalName,

        @NotBlank(message = "{iam.validation.tenant.tax_id.required}")
        @Pattern(regexp = "^(10|15|17|20)\\d{9}$", message = "{iam.validation.tenant.tax_id.format}")
        String taxId,

        @NotBlank(message = "{iam.validation.tenant.admin_email.required}")
        @Email(message = "{iam.validation.user.email.format}")
        @Size(max = 150, message = "{iam.validation.user.email.size}")
        String adminEmail,

        @NotBlank(message = "{iam.validation.tenant.admin_password.required}")
        @Size(min = 8, max = 64, message = "{iam.validation.user.password.size}")
        String adminPassword,

        @NotBlank(message = "{iam.validation.tenant.admin_first_name.required}")
        @Size(max = 100, message = "{iam.validation.user.first_name.size}")
        String adminFirstName,

        @NotBlank(message = "{iam.validation.tenant.admin_last_name.required}")
        @Size(max = 100, message = "{iam.validation.user.last_name.size}")
        String adminLastName,

        @NotBlank(message = "{iam.validation.tenant.admin_phone.required}")
        @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "{iam.validation.user.phone.format}")
        String adminPhone
) {
}
