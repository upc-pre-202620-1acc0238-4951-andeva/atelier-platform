package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

/**
 * REST response resource representing a modular plan feature toggle.
 *
 * @author Joel Huamani Estefanero
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PlanFeatureResource(
        UUID id,
        String featureKey,
        String name,
        String description,
        boolean isEnabled
) {
}
