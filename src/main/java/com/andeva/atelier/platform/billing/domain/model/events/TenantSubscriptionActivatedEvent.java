package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a workshop tenant subscription is activated (either paid or trialing).
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionActivatedEvent(
        SubscriptionId subscriptionId,
        TenantId tenantId,
        PlanId planId,
        Instant expiresAt,
        Instant occurredOn
) implements Serializable {

    public TenantSubscriptionActivatedEvent {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(planId, "PlanId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static TenantSubscriptionActivatedEvent of(SubscriptionId subscriptionId, TenantId tenantId, PlanId planId, Instant expiresAt) {
        return new TenantSubscriptionActivatedEvent(subscriptionId, tenantId, planId, expiresAt, Instant.now());
    }
}
