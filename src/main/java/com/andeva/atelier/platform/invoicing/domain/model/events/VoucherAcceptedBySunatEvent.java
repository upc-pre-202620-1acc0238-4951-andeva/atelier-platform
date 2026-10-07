package com.andeva.atelier.platform.invoicing.domain.model.events;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.DigitalReceiptUrls;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when SUNAT or the authorized PSE confirms acceptance of the electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherAcceptedBySunatEvent(
        VoucherId voucherId,
        TenantId tenantId,
        String digitalSignatureHash,
        DigitalReceiptUrls urls,
        Instant occurredOn
) implements Serializable {

    public VoucherAcceptedBySunatEvent {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        Objects.requireNonNull(tenantId, "Tenant ID cannot be null");
        Objects.requireNonNull(digitalSignatureHash, "Digital signature hash cannot be null");
        Objects.requireNonNull(urls, "Receipt URLs cannot be null");
        Objects.requireNonNull(occurredOn, "OccurredOn timestamp cannot be null");
    }

    public static VoucherAcceptedBySunatEvent of(
            VoucherId voucherId,
            TenantId tenantId,
            String digitalSignatureHash,
            DigitalReceiptUrls urls
    ) {
        return new VoucherAcceptedBySunatEvent(voucherId, tenantId, digitalSignatureHash, urls, Instant.now());
    }
}
