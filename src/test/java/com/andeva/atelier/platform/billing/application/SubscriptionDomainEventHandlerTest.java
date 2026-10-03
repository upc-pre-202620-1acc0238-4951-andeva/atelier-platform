package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.acl.SubscriptionContextFacadeImpl;
import com.andeva.atelier.platform.billing.application.internal.eventhandlers.SubscriptionDomainEventHandler;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionActivatedEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionPastDueEvent;
import com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionRenewedEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.*;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
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
import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link SubscriptionDomainEventHandler}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Subscription Domain Event Handler Tests")
class SubscriptionDomainEventHandlerTest {

    @Mock
    private BillingCachePort billingCachePort;
    @Mock
    private TenantBillingNotificationGatewayPort notificationGatewayPort;
    @Mock
    private StripeGatewayPort stripeGatewayPort;
    @Mock
    private SubscriptionPlanRepository planRepository;
    @Mock
    private TenantSubscriptionRepository subscriptionRepository;

    private SubscriptionDomainEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SubscriptionDomainEventHandler(
                billingCachePort,
                notificationGatewayPort,
                stripeGatewayPort,
                planRepository,
                subscriptionRepository
        );
    }

    @Test
    @DisplayName("Should evict caches and send activation email on TenantSubscriptionActivatedEvent")
    void shouldHandleSubscriptionActivatedEvent() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        Instant trialEnd = Instant.now().plus(Duration.ofDays(14));

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        TenantSubscriptionActivatedEvent event = TenantSubscriptionActivatedEvent.of(subId, tenantId, planId, trialEnd);
        handler.on(event);

        verify(billingCachePort).evict(tenantId);
        verify(notificationGatewayPort).sendSubscriptionActivatedNotification(tenantId, "Pro Plan", trialEnd);
    }

    @Test
    @DisplayName("Should evict caches and send renewal email on TenantSubscriptionRenewedEvent")
    void shouldHandleSubscriptionRenewedEvent() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        Instant periodEnd = Instant.now().plus(Duration.ofDays(30));

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_1"),
                new StripeSubscriptionId("sub_1"),
                SubscriptionPeriod.of(Instant.now(), periodEnd)
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Workshop Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        TenantSubscriptionRenewedEvent event = TenantSubscriptionRenewedEvent.of(subId, tenantId, periodEnd);
        handler.on(event);

        verify(billingCachePort).evict(tenantId);
        verify(notificationGatewayPort).sendSubscriptionRenewedNotification(tenantId, "Pro Workshop Plan", periodEnd);
    }

    @Test
    @DisplayName("Should evict caches and send failure email on TenantSubscriptionPastDueEvent")
    void shouldHandleSubscriptionPastDueEvent() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        Instant graceEnd = Instant.now().plus(Duration.ofDays(5));

        TenantSubscription sub = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_1"),
                new StripeSubscriptionId("sub_1"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(sub));
        when(stripeGatewayPort.createCustomerPortalSession(eq(new StripeCustomerId("cus_1")), anyString()))
                .thenReturn("https://portal.stripe.com/session_1");

        TenantSubscriptionPastDueEvent event = TenantSubscriptionPastDueEvent.of(subId, tenantId, graceEnd);
        handler.on(event);

        verify(billingCachePort).evict(tenantId);
        verify(notificationGatewayPort).sendPaymentFailedNotification(eq(tenantId), contains("past due"), eq("https://portal.stripe.com/session_1"));
    }

    @Test
    @DisplayName("Should evict caches on TenantSubscriptionCanceledEvent")
    void shouldHandleSubscriptionCanceledEvent() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        Instant canceledAt = Instant.now();

        com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionCanceledEvent event =
                com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionCanceledEvent.of(subId, tenantId, canceledAt);

        handler.on(event);

        verify(billingCachePort).evict(tenantId);
        verifyNoInteractions(notificationGatewayPort);
    }
}
