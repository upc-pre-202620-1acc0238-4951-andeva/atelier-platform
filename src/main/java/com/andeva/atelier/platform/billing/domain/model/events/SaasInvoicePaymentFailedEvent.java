package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a recurring SaaS invoice payment attempt fails in Stripe.
 *
 * @author Joel Huamani Estefanero
 */
public record SaasInvoicePaymentFailedEvent(
        SaasInvoiceId invoiceId,
        TenantId tenantId,
        String failureReason,
        Instant occurredOn
) implements Serializable {

    public SaasInvoicePaymentFailedEvent {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
        failureReason = failureReason != null ? failureReason : "Unknown payment failure";
    }

    public static SaasInvoicePaymentFailedEvent of(TenantId tenantId, String failureReason) {
        return new SaasInvoicePaymentFailedEvent(null, tenantId, failureReason, Instant.now());
    }

    public static SaasInvoicePaymentFailedEvent of(SaasInvoiceId invoiceId, TenantId tenantId, String failureReason) {
        return new SaasInvoicePaymentFailedEvent(invoiceId, tenantId, failureReason, Instant.now());
    }
}
