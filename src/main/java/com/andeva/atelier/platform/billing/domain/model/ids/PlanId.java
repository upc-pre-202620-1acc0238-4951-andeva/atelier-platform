package com.andeva.atelier.platform.billing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a commercial subscription plan.
 *
 * @author Joel Huamani Estefanero
 */
public record PlanId(UUID value) implements Serializable {

    public PlanId {
        Objects.requireNonNull(value, "Plan identifier cannot be null");
    }

    public static PlanId of(UUID value) {
        return new PlanId(value);
    }

    public static PlanId of(String value) {
        Objects.requireNonNull(value, "Plan identifier string cannot be null");
        return new PlanId(UUID.fromString(value));
    }

    public static PlanId generate() {
        return new PlanId(UUID.randomUUID());
    }
}
