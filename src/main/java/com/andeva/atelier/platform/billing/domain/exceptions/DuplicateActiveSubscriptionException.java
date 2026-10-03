package com.andeva.atelier.platform.billing.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Thrown when attempting to create or activate a subscription for a tenant that already has an active or trialing subscription.
 *
 * @author Joel Huamani Estefanero
 */
public class DuplicateActiveSubscriptionException extends BillingDomainException {

    public DuplicateActiveSubscriptionException(TenantId tenantId) {
        super("DUPLICATE_ACTIVE_SUBSCRIPTION",
                "An active or trialing subscription already exists for tenant: " + (tenantId != null ? tenantId.value() : "null"));
    }

    public DuplicateActiveSubscriptionException(String message) {
        super("DUPLICATE_ACTIVE_SUBSCRIPTION", message);
    }
}
