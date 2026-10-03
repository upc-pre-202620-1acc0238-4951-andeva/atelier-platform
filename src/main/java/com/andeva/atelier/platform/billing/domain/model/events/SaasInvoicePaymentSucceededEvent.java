package com.andeva.atelier.platform.billing.domain.model.events;

import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted when a SaaS invoice payment successfully completes in Stripe.
 *
 * @author Joel Huamani Estefanero
 */
public record SaasInvoicePaymentSucceededEvent(
        SaasInvoiceId invoiceId,
        TenantId tenantId,
        Money amount,
        Instant occurredOn
) implements Serializable {

    public SaasInvoicePaymentSucceededEvent {
        Objects.requireNonNull(invoiceId, "SaasInvoiceId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(amount, "Money amount cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn timestamp cannot be null");
    }

    public static SaasInvoicePaymentSucceededEvent of(SaasInvoiceId invoiceId, TenantId tenantId, Money amount) {
        return new SaasInvoicePaymentSucceededEvent(invoiceId, tenantId, amount, Instant.now());
    }
}
