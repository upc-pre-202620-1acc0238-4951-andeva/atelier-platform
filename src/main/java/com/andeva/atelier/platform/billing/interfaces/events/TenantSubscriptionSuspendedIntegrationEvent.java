package com.andeva.atelier.platform.billing.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a workshop subscription is definitively suspended or canceled,
 * commanding downstream domains to block access to operations and data mutation endpoints.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantSubscriptionSuspendedIntegrationEvent(
        UUID subscriptionId,
        UUID tenantId,
        String reason,
        Instant occurredOn
) implements Serializable {

    public TenantSubscriptionSuspendedIntegrationEvent {
        Objects.requireNonNull(subscriptionId, "subscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }

    public static TenantSubscriptionSuspendedIntegrationEvent of(
            UUID subscriptionId,
            UUID tenantId,
            String reason
    ) {
        return new TenantSubscriptionSuspendedIntegrationEvent(subscriptionId, tenantId, reason, Instant.now());
    }
}
