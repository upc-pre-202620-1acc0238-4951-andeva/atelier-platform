package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.util.Objects;

/**
 * Command carrying parameters to update commercial terms and quotas of an existing subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public record UpdateSubscriptionPlanCommand(
        PlanId planId,
        String name,
        Money price,
        BillingCycle cycle,
        TenantQuotaLimits quotas
) {

    public UpdateSubscriptionPlanCommand {
        Objects.requireNonNull(planId, "PlanId cannot be null");
        Objects.requireNonNull(name, "Plan name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Plan name cannot be blank");
        }
        Objects.requireNonNull(price, "Plan price cannot be null");
        Objects.requireNonNull(cycle, "BillingCycle cannot be null");
        Objects.requireNonNull(quotas, "TenantQuotaLimits cannot be null");
    }
}
