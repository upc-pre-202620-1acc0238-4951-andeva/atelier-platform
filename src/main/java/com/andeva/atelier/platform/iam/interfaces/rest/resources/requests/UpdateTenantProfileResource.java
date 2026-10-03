package com.andeva.atelier.platform.iam.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request payload for updating the tenant workshop business names.
 *
 * @param name      Commercial or brand trade name
 * @param legalName Official registered corporate name
 * @author Joel Huamani Estefanero
 */
public record UpdateTenantProfileResource(
        @NotBlank(message = "{iam.validation.tenant.business_name.required}")
        @Size(max = 100, message = "{iam.validation.tenant.business_name.size}")
        String name,

        @NotBlank(message = "{iam.validation.tenant.trade_name.required}")
        @Size(max = 150, message = "{iam.validation.tenant.trade_name.size}")
        String legalName
) {
}
