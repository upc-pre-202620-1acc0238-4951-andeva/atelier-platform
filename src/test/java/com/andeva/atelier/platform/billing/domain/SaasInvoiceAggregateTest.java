package com.andeva.atelier.platform.billing.domain;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import com.andeva.atelier.platform.billing.domain.model.events.SaasInvoicePaymentFailedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.SaasInvoicePaymentSucceededEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests covering SaasInvoice aggregate root.
 * Validates invoice creation, payment recording, failure events, voiding, and boundary constraints.
 *
 * @author Joel Huamani Estefanero
 */
class SaasInvoiceAggregateTest {

    @Test
    @DisplayName("Should record paid invoice and emit SaasInvoicePaymentSucceededEvent")
    void shouldRecordPaidInvoice() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvId = StripeInvoiceId.of("in_12345678");
        Money amount = Money.soles(new BigDecimal("299.00"));
        Instant now = Instant.now();

        SaasInvoice invoice = SaasInvoice.recordPaid(
                subId,
                tenantId,
                stripeInvId,
                amount,
                "https://stripe.com/pdf/in_123",
                "https://stripe.com/pay/in_123",
                now
        );

        assertThat(invoice.id()).isNotNull();
        assertThat(invoice.getId()).isEqualTo(invoice.id());
        assertThat(invoice.subscriptionId()).isEqualTo(subId);
        assertThat(invoice.getSubscriptionId()).isEqualTo(subId);
        assertThat(invoice.tenantId()).isEqualTo(tenantId);
        assertThat(invoice.getTenantId()).isEqualTo(tenantId);
        assertThat(invoice.stripeInvoiceId()).isEqualTo(stripeInvId);
        assertThat(invoice.getStripeInvoiceId()).isEqualTo(stripeInvId);
        assertThat(invoice.status()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.amountPaid()).isEqualTo(amount);
        assertThat(invoice.getAmountPaid()).isEqualTo(amount);
        assertThat(invoice.currency()).isEqualTo(Currency.PEN);
        assertThat(invoice.getCurrency()).isEqualTo(Currency.PEN);
        assertThat(invoice.invoicePdfUrl()).isEqualTo("https://stripe.com/pdf/in_123");
        assertThat(invoice.getInvoicePdfUrl()).isEqualTo("https://stripe.com/pdf/in_123");
        assertThat(invoice.hostedInvoiceUrl()).isEqualTo("https://stripe.com/pay/in_123");
        assertThat(invoice.getHostedInvoiceUrl()).isEqualTo("https://stripe.com/pay/in_123");
        assertThat(invoice.paidAt()).contains(now);
        assertThat(invoice.getPaidAt()).isEqualTo(now);

        assertThat(invoice.domainEvents()).hasSize(1);
        Object event = invoice.domainEvents().iterator().next();
        assertThat(event).isInstanceOf(SaasInvoicePaymentSucceededEvent.class);
        SaasInvoicePaymentSucceededEvent succeededEvent = (SaasInvoicePaymentSucceededEvent) event;
        assertThat(succeededEvent.invoiceId()).isEqualTo(invoice.id());
        assertThat(succeededEvent.tenantId()).isEqualTo(tenantId);
        assertThat(succeededEvent.amount()).isEqualTo(amount);
    }

    @Test
    @DisplayName("Should handle null paidAt and null URLs gracefully on recordPaid")
    void shouldHandleNullPaidAtAndUrls() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvId = StripeInvoiceId.of("in_default123");
        Money amount = Money.soles(new BigDecimal("99.00"));

        SaasInvoice invoice = SaasInvoice.recordPaid(
                subId,
                tenantId,
                stripeInvId,
                amount,
                null,
                null,
                null
        );

        assertThat(invoice.paidAt()).isPresent();
        assertThat(invoice.invoicePdfUrl()).isEmpty();
        assertThat(invoice.hostedInvoiceUrl()).isEmpty();
    }

    @Test
    @DisplayName("Should validate required fields in constructor invariants")
    void shouldValidateConstructorInvariants() {
        SaasInvoiceId id = SaasInvoiceId.generate();
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvId = StripeInvoiceId.of("in_inv123");
        Money amount = Money.soles(new BigDecimal("100.00"));

        assertThatThrownBy(() -> new SaasInvoice(null, subId, tenantId, stripeInvId, amount, Currency.PEN, InvoiceStatus.PAID, "", "", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("SaasInvoiceId");

        assertThatThrownBy(() -> new SaasInvoice(id, null, tenantId, stripeInvId, amount, Currency.PEN, InvoiceStatus.PAID, "", "", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("SubscriptionId");

        assertThatThrownBy(() -> new SaasInvoice(id, subId, null, stripeInvId, amount, Currency.PEN, InvoiceStatus.PAID, "", "", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("TenantId");

        assertThatThrownBy(() -> new SaasInvoice(id, subId, tenantId, null, amount, Currency.PEN, InvoiceStatus.PAID, "", "", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("StripeInvoiceId");

        assertThatThrownBy(() -> new SaasInvoice(id, subId, tenantId, stripeInvId, null, Currency.PEN, InvoiceStatus.PAID, "", "", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("amountPaid");

        assertThatThrownBy(() -> new SaasInvoice(id, subId, tenantId, stripeInvId, amount, Currency.PEN, null, "", "", Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("InvoiceStatus");
    }

    @Test
    @DisplayName("Should mark payment failed and emit SaasInvoicePaymentFailedEvent")
    void shouldMarkPaymentFailed() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvId = StripeInvoiceId.of("in_failed123");
        Money amount = Money.soles(new BigDecimal("299.00"));

        SaasInvoice invoice = SaasInvoice.recordPaid(
                subId,
                tenantId,
                stripeInvId,
                amount,
                "",
                "",
                null
        );
        invoice.clearDomainEvents();

        invoice.markPaymentFailed("Insufficient funds on card");

        assertThat(invoice.domainEvents()).hasSize(1);
        Object event = invoice.domainEvents().iterator().next();
        assertThat(event).isInstanceOf(SaasInvoicePaymentFailedEvent.class);
        SaasInvoicePaymentFailedEvent failedEvent = (SaasInvoicePaymentFailedEvent) event;
        assertThat(failedEvent.invoiceId()).isEqualTo(invoice.id());
        assertThat(failedEvent.tenantId()).isEqualTo(tenantId);
        assertThat(failedEvent.failureReason()).isEqualTo("Insufficient funds on card");
    }

    @Test
    @DisplayName("Should transition status to VOID when markVoid is called")
    void shouldMarkVoid() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvId = StripeInvoiceId.of("in_void123");
        Money amount = Money.soles(new BigDecimal("199.00"));

        SaasInvoice invoice = SaasInvoice.recordPaid(
                subId,
                tenantId,
                stripeInvId,
                amount,
                "",
                "",
                null
        );

        assertThat(invoice.status()).isEqualTo(InvoiceStatus.PAID);
        invoice.markVoid();
        assertThat(invoice.status()).isEqualTo(InvoiceStatus.VOID);
    }
}
