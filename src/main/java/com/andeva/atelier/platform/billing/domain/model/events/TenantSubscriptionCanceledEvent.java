package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a workshop tenant subscription is canceled.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionCanceledEvent(
        SubscriptionId subscriptionId,
        TenantId tenantId,
        Instant canceledAt,
        Instant occurredOn
) implements Serializable {

    public TenantSubscriptionCanceledEvent {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(canceledAt, "canceledAt timestamp cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static TenantSubscriptionCanceledEvent of(SubscriptionId subscriptionId, TenantId tenantId, Instant canceledAt) {
        return new TenantSubscriptionCanceledEvent(subscriptionId, tenantId, canceledAt, Instant.now());
    }
}
