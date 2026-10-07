package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when SUNAT or the authorized PSE rejects an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherRejectedBySunatEvent(
        VoucherId voucherId,
        TenantId tenantId,
        String errorCode,
        String errorMessage,
        Instant occurredOn
) implements Serializable {

    public VoucherRejectedBySunatEvent {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(errorCode, "Error code cannot be null");
        Objects.requireNonNull(errorMessage, "Error message cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    public static VoucherRejectedBySunatEvent of(
            VoucherId voucherId,
            TenantId tenantId,
            String errorCode,
            String errorMessage
    ) {
        return new VoucherRejectedBySunatEvent(voucherId, tenantId, errorCode, errorMessage, Instant.now());
    }
}
