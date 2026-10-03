package com.andeva.atelier.platform.billing.domain.repositories;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository contract for managing commercial subscription plans.
 *
 * @author Joel Huamani Estefanero
 */
public interface SubscriptionPlanRepository {

    /**
     * Saves or updates a subscription plan in persistence.
     *
     * @param plan SubscriptionPlan aggregate to persist
     * @return persisted SubscriptionPlan instance
     */
    SubscriptionPlan save(SubscriptionPlan plan);

    /**
     * Finds a plan by its internal unique identifier.
     *
     * @param id PlanId
     * @return Optional containing the plan if found
     */
    Optional<SubscriptionPlan> findById(PlanId id);

    /**
     * Finds a plan by its associated Stripe recurring price identifier.
     *
     * @param stripePriceId StripePriceId
     * @return Optional containing the plan if found
     */
    Optional<SubscriptionPlan> findByStripePriceId(StripePriceId stripePriceId);

    /**
     * Finds all commercial plans currently active and open for tenant onboarding.
     *
     * @return list of active SubscriptionPlans
     */
    List<SubscriptionPlan> findAllActive();
}
