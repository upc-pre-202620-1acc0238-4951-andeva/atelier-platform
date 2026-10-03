package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a new commercial subscription plan is created in the catalog.
 *
 * @author Joel Huamani Estefanero
 */
public record SubscriptionPlanCreatedEvent(
        PlanId planId,
        String name,
        PlanTier tier,
        Money price,
        Instant occurredOn
) implements Serializable {

    public SubscriptionPlanCreatedEvent {
        Objects.requireNonNull(planId, "PlanId cannot be null");
        Objects.requireNonNull(name, "Plan name cannot be null");
        Objects.requireNonNull(tier, "PlanTier cannot be null");
        Objects.requireNonNull(price, "Price cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static SubscriptionPlanCreatedEvent of(PlanId planId, String name, PlanTier tier, Money price) {
        return new SubscriptionPlanCreatedEvent(planId, name, tier, price, Instant.now());
    }
}
