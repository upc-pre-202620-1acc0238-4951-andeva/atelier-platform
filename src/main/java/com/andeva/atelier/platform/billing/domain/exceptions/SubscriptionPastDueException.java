package com.andeva.atelier.platform.billing.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Thrown when an action is denied because the tenant subscription is past due and outside the active grace period.
 *
 * @author Joel Huamani Estefanero
 */
public class SubscriptionPastDueException extends BillingDomainException {

    public SubscriptionPastDueException(TenantId tenantId) {
        super("SUBSCRIPTION_PAST_DUE",
                "Subscription for tenant " + (tenantId != null ? tenantId.value() : "null") + " is past due and the grace period has expired.");
    }

    public SubscriptionPastDueException(String message) {
        super("SUBSCRIPTION_PAST_DUE", message);
    }
}
