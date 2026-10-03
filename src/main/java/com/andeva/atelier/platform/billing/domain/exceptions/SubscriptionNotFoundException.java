package com.andeva.atelier.platform.billing.domain.exceptions;

import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Thrown when a tenant subscription contract cannot be found.
 *
 * @author Joel Huamani Estefanero
 */
public class SubscriptionNotFoundException extends BillingDomainException {

    public SubscriptionNotFoundException(SubscriptionId id) {
        super("SUBSCRIPTION_NOT_FOUND", "Subscription not found with identifier: " + (id != null ? id.value() : "null"));
    }

    public SubscriptionNotFoundException(TenantId tenantId) {
        super("SUBSCRIPTION_NOT_FOUND", "Active subscription not found for tenant: " + (tenantId != null ? tenantId.value() : "null"));
    }

    public SubscriptionNotFoundException(StripeSubscriptionId stripeSubId) {
        super("SUBSCRIPTION_NOT_FOUND", "Subscription not found with Stripe subscription identifier: " + (stripeSubId != null ? stripeSubId.value() : "null"));
    }

    public SubscriptionNotFoundException(String message) {
        super("SUBSCRIPTION_NOT_FOUND", message);
    }
}
