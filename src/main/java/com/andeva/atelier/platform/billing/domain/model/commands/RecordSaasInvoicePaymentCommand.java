package com.andeva.atelier.platform.billing.domain.model.commands;

import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

/**
 * Command carrying parameters to settle and register an invoice receipt from Stripe.
 *
 * @author Joel Huamani Estefanero
 */
public record RecordSaasInvoicePaymentCommand(
        SubscriptionId subscriptionId,
        TenantId tenantId,
        StripeInvoiceId stripeInvoiceId,
        Money amount,
        String pdfUrl,
        String hostedUrl,
        Instant paidAt
) {

    public RecordSaasInvoicePaymentCommand {
        Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(stripeInvoiceId, "StripeInvoiceId cannot be null");
        Objects.requireNonNull(amount, "Money amount cannot be null");
        Objects.requireNonNull(paidAt, "paidAt cannot be null");
    }
}
