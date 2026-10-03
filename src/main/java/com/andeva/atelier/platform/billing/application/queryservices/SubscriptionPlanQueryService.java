package com.andeva.atelier.platform.billing.application.queryservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.ListActivePlansQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;

import java.util.List;
import java.util.Optional;

/**
 * Application Query Service contract for retrieving subscription plans and pricing catalog data.
 *
 * @author Joel Huamani Estefanero
 */
public interface SubscriptionPlanQueryService {

    /**
     * Retrieves a plan by its universal identifier.
     *
     * @param query GetSubscriptionPlanByIdQuery
     * @return Optional containing SubscriptionPlan if found
     */
    Optional<SubscriptionPlan> handle(GetSubscriptionPlanByIdQuery query);

    /**
     * Lists all commercial plans currently active in the platform catalog.
     *
     * @param query ListActivePlansQuery
     * @return list of active SubscriptionPlan aggregates
     */
    List<SubscriptionPlan> handle(ListActivePlansQuery query);

    /**
     * Finds a plan matching an external Stripe Price identifier.
     *
     * @param stripePriceId StripePriceId
     * @return Optional containing SubscriptionPlan if found
     */
    Optional<SubscriptionPlan> findByStripePriceId(StripePriceId stripePriceId);
}
