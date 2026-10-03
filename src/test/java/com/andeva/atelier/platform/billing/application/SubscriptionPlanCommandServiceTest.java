package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.commandservices.SubscriptionPlanCommandService;
import com.andeva.atelier.platform.billing.application.internal.commandservices.SubscriptionPlanCommandServiceImpl;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidPlanPricingException;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.commands.CreateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.UpdateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link SubscriptionPlanCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionPlan Command Service Tests")
class SubscriptionPlanCommandServiceTest {

    @Mock
    private SubscriptionPlanRepository planRepository;

    private SubscriptionPlanCommandService commandService;

    @BeforeEach
    void setUp() {
        commandService = new SubscriptionPlanCommandServiceImpl(planRepository);
    }

    @Test
    @DisplayName("Should successfully create a subscription plan when Stripe price is unique")
    void shouldCreateSubscriptionPlanSuccessfully() {
        StripePriceId priceId = new StripePriceId("price_pro_usd");
        when(planRepository.findByStripePriceId(priceId)).thenReturn(Optional.empty());
        when(planRepository.save(any(SubscriptionPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        Money price = Money.of(new BigDecimal("99.00"), Currency.USD);
        CreateSubscriptionPlanCommand command = new CreateSubscriptionPlanCommand(
                priceId,
                "Pro Workshop",
                PlanTier.PRO,
                price,
                BillingCycle.MONTHLY,
                TenantQuotaLimits.proPlanPreset()
        );

        PlanId result = commandService.handle(command);

        assertThat(result).isNotNull();
        verify(planRepository).save(any(SubscriptionPlan.class));
    }

    @Test
    @DisplayName("Should throw InvalidPlanPricingException if Stripe price ID already exists")
    void shouldThrowIfStripePriceIdExists() {
        StripePriceId priceId = new StripePriceId("price_duplicate");
        SubscriptionPlan existing = new SubscriptionPlan(
                PlanId.generate(),
                priceId,
                "Existing Plan",
                PlanTier.GO,
                PlanPricing.of(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset(),
                true
        );
        when(planRepository.findByStripePriceId(priceId)).thenReturn(Optional.of(existing));

        Money price = Money.of(new BigDecimal("49.00"), Currency.USD);
        CreateSubscriptionPlanCommand command = new CreateSubscriptionPlanCommand(
                priceId,
                "Go Starter",
                PlanTier.GO,
                price,
                BillingCycle.MONTHLY,
                TenantQuotaLimits.goPlanPreset()
        );

        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(InvalidPlanPricingException.class);

        verify(planRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should successfully update details of existing plan")
    void shouldUpdatePlanDetails() {
        PlanId planId = PlanId.generate();
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Old Name",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        Money newPrice = Money.of(new BigDecimal("120.00"), Currency.USD);
        UpdateSubscriptionPlanCommand command = new UpdateSubscriptionPlanCommand(
                planId,
                "Updated Pro Workshop",
                newPrice,
                BillingCycle.MONTHLY,
                TenantQuotaLimits.maxPlanPreset()
        );

        commandService.handle(command);

        assertThat(plan.name()).isEqualTo("Updated Pro Workshop");
        assertThat(plan.pricing().price().amount()).isEqualByComparingTo("120.00");
        verify(planRepository).save(plan);
    }

    @Test
    @DisplayName("Should activate and deactivate subscription plan")
    void shouldActivateAndDeactivatePlan() {
        PlanId planId = PlanId.generate();
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                false
        );

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        commandService.handleActivate(planId);
        assertThat(plan.isActive()).isTrue();

        commandService.handleDeactivate(planId);
        assertThat(plan.isActive()).isFalse();

        verify(planRepository, times(2)).save(plan);
    }

    @Test
    @DisplayName("Should throw PlanNotFoundException when updating non-existent plan")
    void shouldThrowWhenPlanNotFound() {
        PlanId planId = PlanId.generate();
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        Money newPrice = Money.of(new BigDecimal("99.00"), Currency.USD);
        UpdateSubscriptionPlanCommand command = new UpdateSubscriptionPlanCommand(
                planId,
                "New Name",
                newPrice,
                BillingCycle.MONTHLY,
                TenantQuotaLimits.proPlanPreset()
        );

        assertThatThrownBy(() -> commandService.handle(command))
                .isInstanceOf(PlanNotFoundException.class);
    }
}
