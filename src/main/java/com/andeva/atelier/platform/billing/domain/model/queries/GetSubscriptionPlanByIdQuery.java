package com.andeva.atelier.platform.billing.domain.model.queries;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;

import java.util.Objects;

/**
 * Query to retrieve a commercial subscription plan by its unique identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetSubscriptionPlanByIdQuery(
        PlanId planId
) {

    public GetSubscriptionPlanByIdQuery {
        Objects.requireNonNull(planId, "PlanId cannot be null");
    }
}
