package com.andeva.atelier.platform.billing.application.commandservices;

import com.andeva.atelier.platform.billing.domain.model.commands.CreateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.UpdateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;

/**
 * Application Command Service contract for managing commercial software subscription plans.
 *
 * @author Joel Huamani Estefanero
 */
public interface SubscriptionPlanCommandService {

    /**
     * Creates and registers a new commercial subscription plan in the platform catalog.
     *
     * @param command CreateSubscriptionPlanCommand
     * @return newly generated PlanId
     */
    PlanId handle(CreateSubscriptionPlanCommand command);

    /**
     * Updates commercial parameters, pricing, and quota limits of an existing plan.
     *
     * @param command UpdateSubscriptionPlanCommand
     */
    void handle(UpdateSubscriptionPlanCommand command);

    /**
     * Activates a subscription plan, making it available in the public onboarding catalog.
     *
     * @param planId PlanId to activate
     */
    void handleActivate(PlanId planId);

    /**
     * Deactivates a subscription plan, removing it from future sales while preserving existing subscriptions.
     *
     * @param planId PlanId to deactivate
     */
    void handleDeactivate(PlanId planId);
}
