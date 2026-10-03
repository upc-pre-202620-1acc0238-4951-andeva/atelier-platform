package com.andeva.atelier.platform.billing.infrastructure;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
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
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters.SaasInvoiceRepositoryImpl;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters.StripeWebhookEventRepositoryImpl;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters.SubscriptionPlanRepositoryImpl;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters.TenantSubscriptionRepositoryImpl;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.SaasInvoicePersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.StripeWebhookEventPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.SubscriptionPlanPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.TenantSubscriptionPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SaasInvoicePersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.StripeWebhookEventPersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.TenantSubscriptionPersistenceEntity;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.SaasInvoicePersistenceRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.StripeWebhookEventPersistenceRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.SubscriptionPlanPersistenceRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.TenantSubscriptionPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for Billing Repository Adapters.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Billing Repository Adapters Tests")
class BillingRepositoryAdaptersTest {

    @Mock
    private SubscriptionPlanPersistenceRepository planSpringRepo;
    @Mock
    private TenantSubscriptionPersistenceRepository subscriptionSpringRepo;
    @Mock
    private SaasInvoicePersistenceRepository invoiceSpringRepo;
    @Mock
    private StripeWebhookEventPersistenceRepository webhookSpringRepo;

    private final SubscriptionPlanPersistenceAssembler planAssembler = new SubscriptionPlanPersistenceAssembler();
    private final TenantSubscriptionPersistenceAssembler subscriptionAssembler = new TenantSubscriptionPersistenceAssembler();
    private final SaasInvoicePersistenceAssembler invoiceAssembler = new SaasInvoicePersistenceAssembler();
    private final StripeWebhookEventPersistenceAssembler webhookAssembler = new StripeWebhookEventPersistenceAssembler();

    private SubscriptionPlanRepositoryImpl planRepoImpl;
    private TenantSubscriptionRepositoryImpl subscriptionRepoImpl;
    private SaasInvoiceRepositoryImpl invoiceRepoImpl;
    private StripeWebhookEventRepositoryImpl webhookRepoImpl;

    @BeforeEach
    void setUp() {
        planRepoImpl = new SubscriptionPlanRepositoryImpl(planSpringRepo, planAssembler);
        subscriptionRepoImpl = new TenantSubscriptionRepositoryImpl(subscriptionSpringRepo, subscriptionAssembler);
        invoiceRepoImpl = new SaasInvoiceRepositoryImpl(invoiceSpringRepo, invoiceAssembler);
        webhookRepoImpl = new StripeWebhookEventRepositoryImpl(webhookSpringRepo, webhookAssembler);
    }

    @Test
    @DisplayName("SubscriptionPlanRepositoryImpl should save and retrieve plans correctly")
    void testSubscriptionPlanRepositoryImpl() {
        PlanId planId = PlanId.generate();
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_123"),
                "Starter",
                PlanTier.GO,
                PlanPricing.of(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset(),
                Collections.emptyList(),
                true
        );

        when(planSpringRepo.save(any(SubscriptionPlanPersistenceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SubscriptionPlan saved = planRepoImpl.save(plan);
        assertThat(saved).isNotNull();
        assertThat(saved.id()).isEqualTo(planId);

        SubscriptionPlanPersistenceEntity entity = planAssembler.toEntity(plan);
        when(planSpringRepo.findById(planId.value())).thenReturn(Optional.of(entity));
        when(planSpringRepo.findByStripePriceId("price_123")).thenReturn(Optional.of(entity));
        when(planSpringRepo.findAllByIsActiveTrue()).thenReturn(List.of(entity));

        assertThat(planRepoImpl.findById(planId)).isPresent();
        assertThat(planRepoImpl.findByStripePriceId(new StripePriceId("price_123"))).isPresent();
        assertThat(planRepoImpl.findAllActive()).hasSize(1);
    }

    @Test
    @DisplayName("TenantSubscriptionRepositoryImpl should save and retrieve subscriptions correctly")
    void testTenantSubscriptionRepositoryImpl() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        when(subscriptionSpringRepo.save(any(TenantSubscriptionPersistenceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TenantSubscription saved = subscriptionRepoImpl.save(sub);
        assertThat(saved).isNotNull();

        TenantSubscriptionPersistenceEntity entity = subscriptionAssembler.toEntity(sub);
        when(subscriptionSpringRepo.findById(sub.id().value())).thenReturn(Optional.of(entity));
        when(subscriptionSpringRepo.findByTenantId(tenantId.value())).thenReturn(Optional.of(entity));
        when(subscriptionSpringRepo.findByStripeSubscriptionId("sub_123")).thenReturn(Optional.of(entity));
        when(subscriptionSpringRepo.existsByTenantIdAndStatusIn(eq(tenantId.value()), any())).thenReturn(true);
        when(subscriptionSpringRepo.findAllByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(entity));

        assertThat(subscriptionRepoImpl.findById(sub.id())).isPresent();
        assertThat(subscriptionRepoImpl.findByTenantId(tenantId)).isPresent();
        assertThat(subscriptionRepoImpl.findByStripeSubscriptionId(new StripeSubscriptionId("sub_123"))).isPresent();
        assertThat(subscriptionRepoImpl.existsActiveByTenantId(tenantId)).isTrue();
        assertThat(subscriptionRepoImpl.findAllByStatus(SubscriptionStatus.ACTIVE)).hasSize(1);
    }

    @Test
    @DisplayName("SaasInvoiceRepositoryImpl should save and retrieve invoices correctly")
    void testSaasInvoiceRepositoryImpl() {
        SaasInvoiceId invId = SaasInvoiceId.generate();
        TenantId tenantId = TenantId.generate();
        SaasInvoice invoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                tenantId,
                new StripeInvoiceId("in_123"),
                Money.of(new BigDecimal("100.00"), Currency.USD),
                "https://pdf",
                "https://hosted",
                Instant.now()
        );

        when(invoiceSpringRepo.save(any(SaasInvoicePersistenceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SaasInvoice saved = invoiceRepoImpl.save(invoice);
        assertThat(saved).isNotNull();

        SaasInvoicePersistenceEntity entity = invoiceAssembler.toEntity(invoice);
        when(invoiceSpringRepo.findById(invoice.id().value())).thenReturn(Optional.of(entity));
        when(invoiceSpringRepo.findByStripeInvoiceId("in_123")).thenReturn(Optional.of(entity));
        when(invoiceSpringRepo.findAllByTenantIdOrderByCreatedAtDesc(tenantId.value())).thenReturn(List.of(entity));

        assertThat(invoiceRepoImpl.findById(invoice.id())).isPresent();
        assertThat(invoiceRepoImpl.findByStripeInvoiceId(new StripeInvoiceId("in_123"))).isPresent();
        assertThat(invoiceRepoImpl.findAllByTenantId(tenantId)).hasSize(1);
    }

    @Test
    @DisplayName("StripeWebhookEventRepositoryImpl should manage idempotency records correctly")
    void testStripeWebhookEventRepositoryImpl() {
        StripeEventId eventId = new StripeEventId("evt_12345");
        StripeWebhookEvent event = StripeWebhookEvent.receive(eventId, "invoice.paid", "{}");

        when(webhookSpringRepo.save(any(StripeWebhookEventPersistenceEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StripeWebhookEvent saved = webhookRepoImpl.save(event);
        assertThat(saved).isNotNull();

        StripeWebhookEventPersistenceEntity entity = webhookAssembler.toEntity(event);
        when(webhookSpringRepo.findByStripeEventId("evt_12345")).thenReturn(Optional.of(entity));
        when(webhookSpringRepo.existsByStripeEventId("evt_12345")).thenReturn(true);

        assertThat(webhookRepoImpl.findByStripeEventId(eventId)).isPresent();
        assertThat(webhookRepoImpl.existsByStripeEventId(eventId)).isTrue();
    }
}
