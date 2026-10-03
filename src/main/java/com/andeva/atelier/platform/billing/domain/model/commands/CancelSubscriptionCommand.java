package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;

import java.util.Objects;

/**
 * Command carrying parameters to voluntarily cancel a tenant's subscription.
 *
 * @author Joel Huamani Estefanero
 */
public record CancelSubscriptionCommand(
        SubscriptionId subscriptionId,
        boolean cancelImmediately,
        String cancellationReason
) {

    public CancelSubscriptionCommand {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
    }

    public CancelSubscriptionCommand(SubscriptionId subscriptionId, boolean cancelImmediately) {
        this(subscriptionId, cancelImmediately, null);
    }
}
