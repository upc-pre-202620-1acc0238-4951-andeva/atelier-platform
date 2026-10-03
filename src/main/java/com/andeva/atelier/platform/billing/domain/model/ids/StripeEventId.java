package com.andeva.atelier.platform.billing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal immutable identifier for an external Stripe Webhook Event.
 * Enforces the canonical Stripe event ID format prefix (e.g., "evt_...").
 *
 * @author Joel Huamani Estefanero
 */
public record StripeEventId(String value) implements Serializable {

    private static final Pattern STRIPE_EVENT_PATTERN = Pattern.compile("^evt_[a-zA-Z0-9_]+$");

    public StripeEventId {
        Objects.requireNonNull(value, "Stripe event identifier cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Stripe event identifier cannot be blank");
        }
        if (!STRIPE_EVENT_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Stripe event identifier format: " + value);
        }
    }

    public static StripeEventId of(String value) {
        return new StripeEventId(value);
    }
}
