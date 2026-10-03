package com.andeva.atelier.platform.billing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a workshop tenant subscription.
 *
 * @author Joel Huamani Estefanero
 */
public record SubscriptionId(UUID value) implements Serializable {

    public SubscriptionId {
        Objects.requireNonNull(value, "Subscription identifier cannot be null");
    }

    public static SubscriptionId of(UUID value) {
        return new SubscriptionId(value);
    }

    public static SubscriptionId of(String value) {
        Objects.requireNonNull(value, "Subscription identifier string cannot be null");
        return new SubscriptionId(UUID.fromString(value));
    }

    public static SubscriptionId generate() {
        return new SubscriptionId(UUID.randomUUID());
    }
}
