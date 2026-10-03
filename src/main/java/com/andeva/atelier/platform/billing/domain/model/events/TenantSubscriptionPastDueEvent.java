package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a recurring payment charge fails and the subscription enters PAST_DUE state.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionPastDueEvent(
        SubscriptionId subscriptionId,
        TenantId tenantId,
        Instant gracePeriodEnd,
        Instant occurredOn
) implements Serializable {

    public TenantSubscriptionPastDueEvent {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static TenantSubscriptionPastDueEvent of(SubscriptionId subscriptionId, TenantId tenantId, Instant gracePeriodEnd) {
        return new TenantSubscriptionPastDueEvent(subscriptionId, tenantId, gracePeriodEnd, Instant.now());
    }
}
