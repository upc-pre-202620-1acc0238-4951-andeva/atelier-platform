package com.andeva.atelier.platform.billing.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Integration event published when a workshop upgrades their subscription plan,
 * immediately broadcasting expanded operational quotas to MRO, HR, and IoT bounded contexts.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantPlanUpgradedIntegrationEvent(
        UUID subscriptionId,
        UUID tenantId,
        UUID oldPlanId,
        UUID newPlanId,
        String newPlanName,
        String newTier,
        Instant occurredOn
) implements Serializable {

    public TenantPlanUpgradedIntegrationEvent {
        Objects.requireNonNull(subscriptionId, "subscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(newPlanId, "newPlanId cannot be null");
        if (occurredOn == null) {
            occurredOn = Instant.now();
        }
    }

    public static TenantPlanUpgradedIntegrationEvent of(
            UUID subscriptionId,
            UUID tenantId,
            UUID oldPlanId,
            UUID newPlanId,
            String newPlanName,
            String newTier
    ) {
        return new TenantPlanUpgradedIntegrationEvent(subscriptionId, tenantId, oldPlanId, newPlanId, newPlanName, newTier, Instant.now());
    }
}
