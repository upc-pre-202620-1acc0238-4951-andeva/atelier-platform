package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.application.internal.commandservices.TenantSubscriptionCommandServiceImpl;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.DuplicateActiveSubscriptionException;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.exceptions.SubscriptionNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.InitiateCheckoutSessionCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.*;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.interfaces.events.TenantPlanUpgradedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionSuspendedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link TenantSubscriptionCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantSubscription Command Service Tests")
class TenantSubscriptionCommandServiceTest {

    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionPlanRepository planRepository;
    @Mock
    private StripeGatewayPort stripeGatewayPort;
    @Mock
    private BillingCachePort billingCachePort;
    @Mock
    private IamTenantValidationAclPort iamTenantValidationAclPort;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private TenantSubscriptionCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new TenantSubscriptionCommandServiceImpl(
                subscriptionRepository,
                planRepository,
                stripeGatewayPort,
                billingCachePort,
                iamTenantValidationAclPort,
                eventPublisher
        );
    }

    @Test
    @DisplayName("Should successfully initiate checkout session for new customer")
    void shouldInitiateCheckoutSessionForNewCustomer() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();
        StripePriceId priceId = new StripePriceId("price_pro_test");

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                priceId,
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());
        when(iamTenantValidationAclPort.getTenantLegalName(tenantId)).thenReturn("Taller Mecánico SAC");
        when(iamTenantValidationAclPort.getTenantContactEmail(tenantId)).thenReturn("admin@tallermecanico.pe");

        StripeCustomerId customerId = new StripeCustomerId("cus_stripe_123");
        when(stripeGatewayPort.createCustomer(tenantId, "Taller Mecánico SAC", "admin@tallermecanico.pe"))
                .thenReturn(customerId);
        when(stripeGatewayPort.createCheckoutSession(tenantId, planId, priceId, customerId, "https://success", "https://cancel"))
                .thenReturn("https://checkout.stripe.com/pay/cs_test_123");

        InitiateCheckoutSessionCommand command = new InitiateCheckoutSessionCommand(
                tenantId,
                planId,
                "https://success",
                "https://cancel"
        );

        String checkoutUrl = commandService.handle(command);

        assertThat(checkoutUrl).isEqualTo("https://checkout.stripe.com/pay/cs_test_123");
        verify(stripeGatewayPort).createCustomer(tenantId, "Taller Mecánico SAC", "admin@tallermecanico.pe");
    }

    @Test
    @DisplayName("Should throw DuplicateActiveSubscriptionException when tenant already has active plan")
    void shouldThrowWhenDuplicateActiveSubscription() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro_test"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        TenantSubscription existing = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(existing));

        InitiateCheckoutSessionCommand command = new InitiateCheckoutSessionCommand(
                tenantId,
                planId,
                "https://success",
                "https://cancel"
        );

        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(DuplicateActiveSubscriptionException.class);
    }

    @Test
    @DisplayName("Should upgrade subscription plan and publish TenantPlanUpgradedIntegrationEvent")
    void shouldChangeSubscriptionPlan() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId oldPlanId = PlanId.generate();
        PlanId newPlanId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                oldPlanId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        SubscriptionPlan newPlan = new SubscriptionPlan(
                newPlanId,
                new StripePriceId("price_max_test"),
                "Max Plan",
                PlanTier.MAX,
                PlanPricing.of(Money.of(new BigDecimal("199.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.maxPlanPreset(),
                true
        );

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(newPlanId)).thenReturn(Optional.of(newPlan));

        ChangeSubscriptionPlanCommand command = new ChangeSubscriptionPlanCommand(subId, newPlanId);
        commandService.handle(command);

        assertThat(subscription.planId()).isEqualTo(newPlanId);
        verify(subscriptionRepository).save(subscription);
        verify(billingCachePort).evict(tenantId);

        ArgumentCaptor<TenantPlanUpgradedIntegrationEvent> captor = ArgumentCaptor.forClass(TenantPlanUpgradedIntegrationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().newPlanName()).isEqualTo("Max Plan");
        assertThat(captor.getValue().newTier()).isEqualTo("MAX");
    }

    @Test
    @DisplayName("Should cancel subscription immediately and publish TenantSubscriptionSuspendedIntegrationEvent")
    void shouldCancelSubscriptionImmediately() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));

        CancelSubscriptionCommand command = new CancelSubscriptionCommand(subId, true, "Closing shop");
        commandService.handle(command);

        assertThat(subscription.isAccessGranted()).isFalse();
        verify(subscriptionRepository).save(subscription);
        verify(billingCachePort).evict(tenantId);

        ArgumentCaptor<TenantSubscriptionSuspendedIntegrationEvent> captor = ArgumentCaptor.forClass(TenantSubscriptionSuspendedIntegrationEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
    }

    @Test
    @DisplayName("Should cancel subscription at period end without suspending immediately")
    void shouldCancelSubscriptionAtPeriodEnd() {
        SubscriptionId subId = SubscriptionId.generate();
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        when(subscriptionRepository.findById(subId)).thenReturn(Optional.of(subscription));

        CancelSubscriptionCommand command = new CancelSubscriptionCommand(subId, false, "Downgrading later");
        commandService.handle(command);

        assertThat(subscription.isCancelAtPeriodEnd()).isTrue();
        assertThat(subscription.isAccessGranted()).isTrue();
        verify(subscriptionRepository).save(subscription);
        verify(billingCachePort).evict(tenantId);
    }

    @Test
    @DisplayName("Should start free trial for tenant and cache active status")
    void shouldStartFreeTrial() {
        TenantId tenantId = TenantId.generate();
        PlanId planId = PlanId.generate();

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_go_test"),
                "Go Starter",
                PlanTier.GO,
                PlanPricing.of(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset(),
                true
        );

        when(subscriptionRepository.existsActiveByTenantId(tenantId)).thenReturn(false);
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(iamTenantValidationAclPort.getTenantLegalName(tenantId)).thenReturn("Taller Nuevo SAC");
        when(iamTenantValidationAclPort.getTenantContactEmail(tenantId)).thenReturn("contacto@tallernuevo.pe");
        when(stripeGatewayPort.createCustomer(tenantId, "Taller Nuevo SAC", "contacto@tallernuevo.pe"))
                .thenReturn(new StripeCustomerId("cus_new_999"));
        when(subscriptionRepository.save(any(TenantSubscription.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantSubscription trial = commandService.handleStartTrial(tenantId, planId, 14);

        assertThat(trial).isNotNull();
        assertThat(trial.isAccessGranted()).isTrue();
        verify(billingCachePort).cacheSubscriptionActive(tenantId, true);
    }
}
