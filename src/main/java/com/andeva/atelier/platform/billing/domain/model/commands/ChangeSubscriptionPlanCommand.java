package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;

import java.util.Objects;

/**
 * Command carrying parameters to upgrade or downgrade a workshop's subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public record ChangeSubscriptionPlanCommand(
        SubscriptionId subscriptionId,
        PlanId newPlanId,
        boolean prorate
) {

    public ChangeSubscriptionPlanCommand {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(newPlanId, "New PlanId cannot be null");
    }

    public ChangeSubscriptionPlanCommand(SubscriptionId subscriptionId, PlanId newPlanId) {
        this(subscriptionId, newPlanId, true);
    }
}
