package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a recurring subscription payment succeeds and the period is renewed.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionRenewedEvent(
        SubscriptionId subscriptionId,
        TenantId tenantId,
        Instant newPeriodEnd,
        Instant occurredOn
) implements Serializable {

    public TenantSubscriptionRenewedEvent {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(newPeriodEnd, "newPeriodEnd cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static TenantSubscriptionRenewedEvent of(SubscriptionId subscriptionId, TenantId tenantId, Instant newPeriodEnd) {
        return new TenantSubscriptionRenewedEvent(subscriptionId, tenantId, newPeriodEnd, Instant.now());
    }
}
