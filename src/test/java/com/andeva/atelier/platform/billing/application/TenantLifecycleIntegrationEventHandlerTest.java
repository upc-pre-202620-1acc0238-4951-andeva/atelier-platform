package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.application.internal.eventhandlers.TenantLifecycleIntegrationEventHandler;
import com.andeva.atelier.platform.billing.domain.exceptions.DuplicateActiveSubscriptionException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.iam.interfaces.events.TenantCreatedIntegrationEvent;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link TenantLifecycleIntegrationEventHandler}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantLifecycle Integration Event Handler Tests")
class TenantLifecycleIntegrationEventHandlerTest {

    @Mock
    private TenantSubscriptionCommandService tenantSubscriptionCommandService;
    @Mock
    private SubscriptionPlanRepository planRepository;

    private TenantLifecycleIntegrationEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TenantLifecycleIntegrationEventHandler(tenantSubscriptionCommandService, planRepository);
    }

    @Test
    @DisplayName("Should start 14-day free trial on GO plan when TenantCreatedIntegrationEvent is received")
    void shouldStartFreeTrialOnTenantCreatedEvent() {
        UUID tenantUuid = UUID.randomUUID();
        PlanId planId = PlanId.generate();

        SubscriptionPlan goPlan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_go"),
                "Go Starter",
                PlanTier.GO,
                PlanPricing.of(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset(),
                true
        );

        when(planRepository.findAllActive()).thenReturn(List.of(goPlan));

        TenantCreatedIntegrationEvent event = new TenantCreatedIntegrationEvent(
                tenantUuid,
                "Taller Veloz",
                "Taller Veloz SAC",
                "20601234567",
                Instant.now()
        );

        handler.on(event);

        verify(tenantSubscriptionCommandService).handleStartTrial(new TenantId(tenantUuid), planId, 14);
    }

    @Test
    @DisplayName("Should handle DuplicateActiveSubscriptionException gracefully without throwing")
    void shouldHandleDuplicateActiveGracefully() {
        UUID tenantUuid = UUID.randomUUID();
        PlanId planId = PlanId.generate();

        SubscriptionPlan goPlan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_go"),
                "Go Starter",
                PlanTier.GO,
                PlanPricing.of(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset(),
                true
        );

        when(planRepository.findAllActive()).thenReturn(List.of(goPlan));
        doThrow(new DuplicateActiveSubscriptionException(new TenantId(tenantUuid)))
                .when(tenantSubscriptionCommandService).handleStartTrial(new TenantId(tenantUuid), planId, 14);

        TenantCreatedIntegrationEvent event = new TenantCreatedIntegrationEvent(
                tenantUuid,
                "Taller Veloz",
                "Taller Veloz SAC",
                "20601234567",
                Instant.now()
        );

        // Does not throw
        handler.on(event);

        verify(tenantSubscriptionCommandService).handleStartTrial(new TenantId(tenantUuid), planId, 14);
    }
}
