package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable value object encapsulating the price and recurring billing cycle of a plan.
 *
 * @author Joel Huamani Estefanero
 */
public record PlanPricing(
        Money price,
        BillingCycle billingCycle
) implements Serializable {

    public PlanPricing {
        Objects.requireNonNull(price, "Plan price cannot be null");
        Objects.requireNonNull(billingCycle, "Billing cycle cannot be null");
        if (price.amount().signum() < 0) {
            throw new IllegalArgumentException("Plan price amount cannot be negative");
        }
    }

    public static PlanPricing of(Money price, BillingCycle billingCycle) {
        return new PlanPricing(price, billingCycle);
    }
}
