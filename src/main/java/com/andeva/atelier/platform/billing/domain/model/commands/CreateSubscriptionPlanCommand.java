package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.util.Objects;

/**
 * Command carrying parameters to register a new commercial software subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public record CreateSubscriptionPlanCommand(
        StripePriceId stripePriceId,
        String name,
        PlanTier tier,
        Money price,
        BillingCycle cycle,
        TenantQuotaLimits quotas
) {

    public CreateSubscriptionPlanCommand {
        Objects.requireNonNull(stripePriceId, "StripePriceId cannot be null");
        Objects.requireNonNull(name, "Plan name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Plan name cannot be blank");
        }
        Objects.requireNonNull(tier, "PlanTier cannot be null");
        Objects.requireNonNull(price, "Plan price cannot be null");
        Objects.requireNonNull(cycle, "BillingCycle cannot be null");
        Objects.requireNonNull(quotas, "TenantQuotaLimits cannot be null");
    }
}
