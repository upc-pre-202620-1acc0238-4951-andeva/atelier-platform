package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal immutable identifier for a Stripe Price catalog object.
 * Enforces the canonical Stripe price ID format prefix (e.g., "price_...").
 *
 * @author Joel Huamani Estefanero
 */
public record StripePriceId(String value) implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("^price_[a-zA-Z0-9_]+$");

    public StripePriceId {
        Objects.requireNonNull(value, "Stripe price identifier cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Stripe price identifier cannot be blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Stripe price identifier format: " + value);
        }
    }

    public static StripePriceId of(String value) {
        return new StripePriceId(value);
    }
}
