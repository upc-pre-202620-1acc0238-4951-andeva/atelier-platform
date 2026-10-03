package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal immutable identifier for a Stripe Subscription.
 * Enforces the canonical Stripe subscription ID format prefix (e.g., "sub_...").
 *
 * @author Joel Huamani Estefanero
 */
public record StripeSubscriptionId(String value) implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("^sub_[a-zA-Z0-9_]+$");

    public StripeSubscriptionId {
        Objects.requireNonNull(value, "Stripe subscription identifier cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Stripe subscription identifier cannot be blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Stripe subscription identifier format: " + value);
        }
    }

    public static StripeSubscriptionId of(String value) {
        return new StripeSubscriptionId(value);
    }
}
