package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal immutable identifier for a Stripe Customer account.
 * Enforces the canonical Stripe customer ID format prefix (e.g., "cus_...").
 *
 * @author Joel Huamani Estefanero
 */
public record StripeCustomerId(String value) implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("^cus_[a-zA-Z0-9_]+$");

    public StripeCustomerId {
        Objects.requireNonNull(value, "Stripe customer identifier cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Stripe customer identifier cannot be blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Stripe customer identifier format: " + value);
        }
    }

    public static StripeCustomerId of(String value) {
        return new StripeCustomerId(value);
    }
}
