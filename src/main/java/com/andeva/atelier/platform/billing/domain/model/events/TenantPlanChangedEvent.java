package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a workshop upgrades or downgrades to a different subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public record TenantPlanChangedEvent(
        SubscriptionId subscriptionId,
        TenantId tenantId,
        PlanId oldPlanId,
        PlanId newPlanId,
        Instant occurredOn
) implements Serializable {

    public TenantPlanChangedEvent {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(oldPlanId, "oldPlanId cannot be null");
        Objects.requireNonNull(newPlanId, "newPlanId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static TenantPlanChangedEvent of(SubscriptionId subscriptionId, TenantId tenantId, PlanId oldPlanId, PlanId newPlanId) {
        return new TenantPlanChangedEvent(subscriptionId, tenantId, oldPlanId, newPlanId, Instant.now());
    }
}
