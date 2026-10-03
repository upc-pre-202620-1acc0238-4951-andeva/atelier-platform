package com.andeva.atelier.platform.billing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a SaaS billing invoice receipt.
 *
 * @author Joel Huamani Estefanero
 */
public record SaasInvoiceId(UUID value) implements Serializable {

    public SaasInvoiceId {
        Objects.requireNonNull(value, "Saas invoice identifier cannot be null");
    }

    public static SaasInvoiceId of(UUID value) {
        return new SaasInvoiceId(value);
    }

    public static SaasInvoiceId of(String value) {
        Objects.requireNonNull(value, "Saas invoice identifier string cannot be null");
        return new SaasInvoiceId(UUID.fromString(value));
    }

    public static SaasInvoiceId generate() {
        return new SaasInvoiceId(UUID.randomUUID());
    }
}
