package com.andeva.atelier.platform.billing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a commercial plan feature module.
 *
 * @author Joel Huamani Estefanero
 */
public record PlanFeatureId(UUID value) implements Serializable {

    public PlanFeatureId {
        Objects.requireNonNull(value, "Plan feature identifier cannot be null");
    }

    public static PlanFeatureId of(UUID value) {
        return new PlanFeatureId(value);
    }

    public static PlanFeatureId of(String value) {
        Objects.requireNonNull(value, "Plan feature identifier string cannot be null");
        return new PlanFeatureId(UUID.fromString(value));
    }

    public static PlanFeatureId generate() {
        return new PlanFeatureId(UUID.randomUUID());
    }
}
