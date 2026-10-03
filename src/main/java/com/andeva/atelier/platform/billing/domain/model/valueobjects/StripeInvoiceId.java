package com.andeva.atelier.platform.billing.domain.model.valueobjects;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Universal immutable identifier for a Stripe Invoice object.
 * Enforces the canonical Stripe invoice ID format prefix (e.g., "in_...").
 *
 * @author Joel Huamani Estefanero
 */
public record StripeInvoiceId(String value) implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("^in_[a-zA-Z0-9_]+$");

    public StripeInvoiceId {
        Objects.requireNonNull(value, "Stripe invoice identifier cannot be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Stripe invoice identifier cannot be blank");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid Stripe invoice identifier format: " + value);
        }
    }

    public static StripeInvoiceId of(String value) {
        return new StripeInvoiceId(value);
    }
}
