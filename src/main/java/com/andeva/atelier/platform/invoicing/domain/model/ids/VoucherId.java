package com.andeva.atelier.platform.invoicing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for an Electronic Voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherId(UUID value) implements Serializable {

    public VoucherId {
        Objects.requireNonNull(value, "Voucher ID cannot be null");
    }

    public static VoucherId of(UUID value) {
        return new VoucherId(value);
    }

    public static VoucherId of(String value) {
        Objects.requireNonNull(value, "Voucher ID string cannot be null");
        return new VoucherId(UUID.fromString(value));
    }

    public static VoucherId generate() {
        return new VoucherId(UUID.randomUUID());
    }
}
