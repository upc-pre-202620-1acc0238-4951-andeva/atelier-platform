package com.andeva.atelier.platform.billing.infrastructure;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.InvoiceStatus;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.enums.WebhookProcessingStatus;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.SaasInvoicePersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.StripeWebhookEventPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.SubscriptionPlanPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.TenantSubscriptionPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SaasInvoicePersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.StripeWebhookEventPersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.TenantSubscriptionPersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test suite for Billing Persistence Assemblers.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("Billing Persistence Assemblers Tests")
class BillingPersistenceAssemblersTest {

    private final SubscriptionPlanPersistenceAssembler planAssembler = new SubscriptionPlanPersistenceAssembler();
    private final TenantSubscriptionPersistenceAssembler subscriptionAssembler = new TenantSubscriptionPersistenceAssembler();
    private final SaasInvoicePersistenceAssembler invoiceAssembler = new SaasInvoicePersistenceAssembler();
    private final StripeWebhookEventPersistenceAssembler webhookAssembler = new StripeWebhookEventPersistenceAssembler();

    @Test
    @DisplayName("SubscriptionPlanPersistenceAssembler should map bidirectional accurately")
    void testSubscriptionPlanAssembler() {
        PlanId planId = PlanId.generate();
        StripePriceId priceId = new StripePriceId("price_pro_123");
        PlanPricing pricing = PlanPricing.of(Money.of(new BigDecimal("199.00"), Currency.USD), BillingCycle.MONTHLY);
        TenantQuotaLimits quotas = TenantQuotaLimits.proPlanPreset();

        PlanFeature feature = PlanFeature.create(planId, "ai_diagnostics", "AI Diagnostics tool", true);
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                priceId,
                "Pro Garage",
                PlanTier.PRO,
                pricing,
                quotas,
                List.of(feature),
                true
        );

        SubscriptionPlanPersistenceEntity entity = planAssembler.toEntity(plan);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(planId.value());
        assertThat(entity.getStripePriceId()).isEqualTo("price_pro_123");
        assertThat(entity.getName()).isEqualTo("Pro Garage");
        assertThat(entity.getTier()).isEqualTo(PlanTier.PRO);
        assertThat(entity.getPrice()).isEqualByComparingTo("199.00");
        assertThat(entity.getFeatures()).hasSize(1);

        SubscriptionPlan domain = planAssembler.toDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.id()).isEqualTo(planId);
        assertThat(domain.name()).isEqualTo("Pro Garage");
        assertThat(domain.tier()).isEqualTo(PlanTier.PRO);
        assertThat(domain.pricing().price().amount()).isEqualByComparingTo("199.00");
        assertThat(domain.features()).hasSize(1);
        assertThat(domain.features().get(0).featureKey()).isEqualTo("AI_DIAGNOSTICS");
    }

    @Test
    @DisplayName("TenantSubscriptionPersistenceAssembler should map bidirectional accurately")
    void testTenantSubscriptionAssembler() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripeCustomerId customerId = new StripeCustomerId("cus_123");
        StripeSubscriptionId stripeSubId = new StripeSubscriptionId("sub_123");
        Instant start = Instant.now().minus(Duration.ofDays(10));
        Instant end = Instant.now().plus(Duration.ofDays(20));
        SubscriptionPeriod period = SubscriptionPeriod.of(start, end);

        TenantSubscription subscription = new TenantSubscription(
                subId,
                tenantId,
                planId,
                customerId,
                stripeSubId,
                SubscriptionStatus.ACTIVE,
                period,
                false,
                null,
                null
        );

        TenantSubscriptionPersistenceEntity entity = subscriptionAssembler.toEntity(subscription);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(subId.value());
        assertThat(entity.getTenantId()).isEqualTo(tenantId.value());
        assertThat(entity.getPlanId()).isEqualTo(planId.value());
        assertThat(entity.getStripeCustomerId()).isEqualTo("cus_123");
        assertThat(entity.getStripeSubscriptionId()).isEqualTo("sub_123");
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);

        TenantSubscription domain = subscriptionAssembler.toDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.id()).isEqualTo(subId);
        assertThat(domain.tenantId()).isEqualTo(tenantId);
        assertThat(domain.planId()).isEqualTo(planId);
        assertThat(domain.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(domain.currentPeriod().startDate()).isEqualTo(start);
        assertThat(domain.currentPeriod().endDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("SaasInvoicePersistenceAssembler should map bidirectional accurately")
    void testSaasInvoiceAssembler() {
        SaasInvoiceId invoiceId = SaasInvoiceId.generate();
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        StripeInvoiceId stripeInvoiceId = new StripeInvoiceId("in_123456");
        Money amount = Money.of(new BigDecimal("99.50"), Currency.USD);
        Instant paidAt = Instant.now();

        SaasInvoice invoice = new SaasInvoice(
                invoiceId,
                subId,
                tenantId,
                stripeInvoiceId,
                amount,
                Currency.USD,
                InvoiceStatus.PAID,
                "https://pdf.url",
                "https://hosted.url",
                paidAt
        );

        SaasInvoicePersistenceEntity entity = invoiceAssembler.toEntity(invoice);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(invoiceId.value());
        assertThat(entity.getSubscriptionId()).isEqualTo(subId.value());
        assertThat(entity.getTenantId()).isEqualTo(tenantId.value());
        assertThat(entity.getStripeInvoiceId()).isEqualTo("in_123456");
        assertThat(entity.getAmountPaid()).isEqualByComparingTo("99.50");
        assertThat(entity.getStatus()).isEqualTo(InvoiceStatus.PAID);

        SaasInvoice domain = invoiceAssembler.toDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.id()).isEqualTo(invoiceId);
        assertThat(domain.stripeInvoiceId()).isEqualTo(stripeInvoiceId);
        assertThat(domain.amountPaid().amount()).isEqualByComparingTo("99.50");
        assertThat(domain.status()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    @DisplayName("StripeWebhookEventPersistenceAssembler should map bidirectional accurately")
    void testStripeWebhookEventAssembler() {
        UUID eventUuid = UUID.randomUUID();
        StripeEventId eventId = new StripeEventId("evt_test_123");
        Instant processedAt = Instant.now();

        StripeWebhookEvent event = new StripeWebhookEvent(
                eventUuid,
                eventId,
                "invoice.payment_succeeded",
                "{\"id\":\"evt_test_123\"}",
                WebhookProcessingStatus.PROCESSED,
                processedAt,
                null
        );

        StripeWebhookEventPersistenceEntity entity = webhookAssembler.toEntity(event);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(eventUuid);
        assertThat(entity.getStripeEventId()).isEqualTo("evt_test_123");
        assertThat(entity.getType()).isEqualTo("invoice.payment_succeeded");
        assertThat(entity.getStatus()).isEqualTo(WebhookProcessingStatus.PROCESSED);

        StripeWebhookEvent domain = webhookAssembler.toDomain(entity);
        assertThat(domain).isNotNull();
        assertThat(domain.id()).isEqualTo(eventUuid);
        assertThat(domain.stripeEventId()).isEqualTo(eventId);
        assertThat(domain.eventType()).isEqualTo("invoice.payment_succeeded");
        assertThat(domain.status()).isEqualTo(WebhookProcessingStatus.PROCESSED);
    }
}
