package com.andeva.atelier.platform.billing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload to upgrade or downgrade the active commercial plan of a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public record ChangeSubscriptionPlanRequest(
        @NotNull(message = "{billing.validation.plan.id.required}")
        UUID newPlanId,

        Boolean prorate
) {
    public ChangeSubscriptionPlanRequest {
        if (prorate == null) {
            prorate = true;
        }
    }

    public ChangeSubscriptionPlanRequest(UUID newPlanId) {
        this(newPlanId, true);
    }
}
