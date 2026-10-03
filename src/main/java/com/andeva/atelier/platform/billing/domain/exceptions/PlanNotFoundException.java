package com.andeva.atelier.platform.billing.domain.exceptions;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;

/**
 * Thrown when a commercial subscription plan is not found in the catalog.
 *
 * @author Joel Huamani Estefanero
 */
public class PlanNotFoundException extends BillingDomainException {

    public PlanNotFoundException(PlanId planId) {
        super("PLAN_NOT_FOUND", "Subscription plan not found with identifier: " + (planId != null ? planId.value() : "null"));
    }

    public PlanNotFoundException(StripePriceId stripePriceId) {
        super("PLAN_NOT_FOUND", "Subscription plan not found with Stripe price identifier: " + (stripePriceId != null ? stripePriceId.value() : "null"));
    }

    public PlanNotFoundException(String message) {
        super("PLAN_NOT_FOUND", message);
    }
}
