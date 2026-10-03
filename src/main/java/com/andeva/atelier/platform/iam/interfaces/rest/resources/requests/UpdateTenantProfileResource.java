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
        @NotBlank @Size(max = 100)
        String name,

        @NotBlank @Size(max = 150)
        String legalName
) {
}
