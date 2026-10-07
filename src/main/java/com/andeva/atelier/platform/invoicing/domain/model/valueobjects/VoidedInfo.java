package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Audit record of an electronic voucher cancellation (comunicación de baja).
 *
 * @author Joel Huamani Estefanero
 */
public record VoidedInfo(
        String reason,
        Instant voidedAt
) implements Serializable {

    public VoidedInfo {
        Objects.requireNonNull(reason, "Void reason cannot be null");
        Objects.requireNonNull(voidedAt, "Void timestamp cannot be null");
        reason = reason.trim();
        if (reason.isBlank()) {
            throw new IllegalArgumentException("Void reason cannot be blank");
        }
    }

    public static VoidedInfo of(String reason, Instant voidedAt) {
        return new VoidedInfo(reason, voidedAt);
    }

    public static VoidedInfo of(String reason) {
        return new VoidedInfo(reason, Instant.now());
    }
}
