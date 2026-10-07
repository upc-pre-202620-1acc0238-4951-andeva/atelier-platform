package com.andeva.atelier.platform.invoicing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a Voucher Payment settlement.
 *
 * @author Joel Huamani Estefanero
 */
public record PaymentId(UUID value) implements Serializable {

    public PaymentId {
        Objects.requireNonNull(value, "Payment ID cannot be null");
    }

    public static PaymentId of(UUID value) {
        return new PaymentId(value);
    }

    public static PaymentId of(String value) {
        Objects.requireNonNull(value, "Payment ID string cannot be null");
        return new PaymentId(UUID.fromString(value));
    }

    public static PaymentId generate() {
        return new PaymentId(UUID.randomUUID());
    }
}
