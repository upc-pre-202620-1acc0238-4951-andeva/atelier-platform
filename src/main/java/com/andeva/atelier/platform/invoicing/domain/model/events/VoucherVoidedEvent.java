package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when an electronic voucher is voided or cancelled.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherVoidedEvent(
        VoucherId voucherId,
        TenantId tenantId,
        String reason,
        Instant occurredOn
) implements Serializable {

    public VoucherVoidedEvent {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(reason, "Void reason cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    public static VoucherVoidedEvent of(VoucherId voucherId, TenantId tenantId, String reason) {
        return new VoucherVoidedEvent(voucherId, tenantId, reason, Instant.now());
    }
}
