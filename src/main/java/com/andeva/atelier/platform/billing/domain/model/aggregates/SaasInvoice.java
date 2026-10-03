package com.andeva.atelier.platform.billing.domain.model.aggregates;

import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import com.andeva.atelier.platform.billing.domain.model.events.SaasInvoicePaymentFailedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.SaasInvoicePaymentSucceededEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Aggregate Root representing a formal recurring SaaS invoice receipt issued to a workshop tenant.
 *
 * @author Joel Huamani Estefanero
 */
public class SaasInvoice extends AbstractDomainAggregateRoot<SaasInvoice> {

    private final SaasInvoiceId id;
    private final SubscriptionId subscriptionId;
    private final TenantId tenantId;
    private final StripeInvoiceId stripeInvoiceId;
    private final Money amountPaid;
    private final Currency currency;
    private InvoiceStatus status;
    private final String invoicePdfUrl;
    private final String hostedInvoiceUrl;
    private Instant paidAt;

    public SaasInvoice(
            SaasInvoiceId id,
            SubscriptionId subscriptionId,
            TenantId tenantId,
            StripeInvoiceId stripeInvoiceId,
            Money amountPaid,
            Currency currency,
            InvoiceStatus status,
            String invoicePdfUrl,
            String hostedInvoiceUrl,
            Instant paidAt
    ) {
        this.id = Objects.requireNonNull(id, "SaasInvoiceId cannot be null");
        this.subscriptionId = Objects.requireNonNull(subscriptionId, "SubscriptionId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.stripeInvoiceId = Objects.requireNonNull(stripeInvoiceId, "StripeInvoiceId cannot be null");
        this.amountPaid = Objects.requireNonNull(amountPaid, "amountPaid cannot be null");
        this.currency = currency != null ? currency : amountPaid.currency();
        this.status = Objects.requireNonNull(status, "InvoiceStatus cannot be null");
        this.invoicePdfUrl = invoicePdfUrl != null ? invoicePdfUrl : "";
        this.hostedInvoiceUrl = hostedInvoiceUrl != null ? hostedInvoiceUrl : "";
        this.paidAt = paidAt;
    }

    public static SaasInvoice recordPaid(
            SubscriptionId subscriptionId,
            TenantId tenantId,
            StripeInvoiceId stripeInvoiceId,
            Money amountPaid,
            String pdfUrl,
            String hostedUrl,
            Instant paidAt
    ) {
        SaasInvoiceId invoiceId = SaasInvoiceId.generate();
        Instant paidTimestamp = paidAt != null ? paidAt : Instant.now();
        SaasInvoice invoice = new SaasInvoice(
                invoiceId,
                subscriptionId,
                tenantId,
                stripeInvoiceId,
                amountPaid,
                amountPaid.currency(),
                InvoiceStatus.PAID,
                pdfUrl,
                hostedUrl,
                paidTimestamp
        );
        invoice.registerDomainEvent(SaasInvoicePaymentSucceededEvent.of(invoiceId, tenantId, amountPaid));
        return invoice;
    }

    public void markPaymentFailed(String reason) {
        this.status = InvoiceStatus.UNCOLLECTIBLE;
        registerDomainEvent(SaasInvoicePaymentFailedEvent.of(this.id, this.tenantId, reason));
    }

    public void markVoid() {
        this.status = InvoiceStatus.VOID;
    }

    public SaasInvoiceId id() {
        return id;
    }

    public SaasInvoiceId getId() {
        return id;
    }

    public SubscriptionId subscriptionId() {
        return subscriptionId;
    }

    public SubscriptionId getSubscriptionId() {
        return subscriptionId;
    }

    public TenantId tenantId() {
        return tenantId;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public StripeInvoiceId stripeInvoiceId() {
        return stripeInvoiceId;
    }

    public StripeInvoiceId getStripeInvoiceId() {
        return stripeInvoiceId;
    }

    public Money amountPaid() {
        return amountPaid;
    }

    public Money getAmountPaid() {
        return amountPaid;
    }

    public Currency currency() {
        return currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public InvoiceStatus status() {
        return status;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public String invoicePdfUrl() {
        return invoicePdfUrl;
    }

    public String getInvoicePdfUrl() {
        return invoicePdfUrl;
    }

    public String hostedInvoiceUrl() {
        return hostedInvoiceUrl;
    }

    public String getHostedInvoiceUrl() {
        return hostedInvoiceUrl;
    }

    public Optional<Instant> paidAt() {
        return Optional.ofNullable(paidAt);
    }

    public Instant getPaidAt() {
        return paidAt;
    }
}
