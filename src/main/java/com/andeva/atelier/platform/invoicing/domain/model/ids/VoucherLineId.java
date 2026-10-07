package com.andeva.atelier.platform.invoicing.domain.model.ids;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Universal immutable identifier for a detail line within an Electronic Voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherLineId(UUID value) implements Serializable {

    public VoucherLineId {
        Objects.requireNonNull(value, "Voucher line ID cannot be null");
    }

    public static VoucherLineId of(UUID value) {
        return new VoucherLineId(value);
    }

    public static VoucherLineId of(String value) {
        Objects.requireNonNull(value, "Voucher line ID string cannot be null");
        return new VoucherLineId(UUID.fromString(value));
    }

    public static VoucherLineId generate() {
        return new VoucherLineId(UUID.randomUUID());
    }
}
