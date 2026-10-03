package com.andeva.atelier.platform.billing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO representing a packaged modular capability feature within a commercial plan.
 *
 * @author Joel Huamani Estefanero
 */
public record PlanFeatureRequest(
        @NotBlank(message = "{billing.validation.plan.feature_key.required}")
        String featureKey,

        @NotBlank(message = "{billing.validation.plan.feature_name.required}")
        String name,

        String description,

        boolean isEnabled
) {
}
