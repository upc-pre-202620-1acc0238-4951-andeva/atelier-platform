package com.andeva.atelier.platform.billing.domain.exceptions;

/**
 * Thrown when pricing configuration, amount, or billing cycle violates domain invariants.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidPlanPricingException extends BillingDomainException {

    public InvalidPlanPricingException(String message) {
        super("INVALID_PLAN_PRICING", message);
    }
}
