package com.andeva.atelier.platform.billing.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a workshop tenant's subscription status transitions
 * (e.g. to ACTIVE, PAST_DUE, or CANCELED), requiring cross-context cache invalidation and ACL synchronization.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionStatusChangedIntegrationEvent(
        UUID subscriptionId,
        UUID tenantId,
        String oldStatus,
        String newStatus,
        Instant occurredOn
) implements Serializable {

    public TenantSubscriptionStatusChangedIntegrationEvent {
        Objects.requireNonNull(subscriptionId, "subscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(newStatus, "newStatus cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }

    public static TenantSubscriptionStatusChangedIntegrationEvent of(
            UUID subscriptionId,
            UUID tenantId,
            String oldStatus,
            String newStatus
    ) {
        return new TenantSubscriptionStatusChangedIntegrationEvent(subscriptionId, tenantId, oldStatus, newStatus, Instant.now());
    }
}
