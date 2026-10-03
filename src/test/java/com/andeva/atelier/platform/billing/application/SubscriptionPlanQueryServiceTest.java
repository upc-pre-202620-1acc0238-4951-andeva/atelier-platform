package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.internal.queryservices.SubscriptionPlanQueryServiceImpl;
import com.andeva.atelier.platform.billing.application.queryservices.SubscriptionPlanQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.ListActivePlansQuery;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite for {@link SubscriptionPlanQueryServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionPlan Query Service Tests")
class SubscriptionPlanQueryServiceTest {

    @Mock
    private SubscriptionPlanRepository planRepository;

    private SubscriptionPlanQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new SubscriptionPlanQueryServiceImpl(planRepository);
    }

    @Test
    @DisplayName("Should find plan by ID")
    void shouldFindPlanById() {
        PlanId planId = PlanId.generate();
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_123"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        Optional<SubscriptionPlan> result = queryService.handle(new GetSubscriptionPlanByIdQuery(planId));

        assertThat(result).isPresent();
        assertThat(result.get().name()).isEqualTo("Pro Plan");
    }

    @Test
    @DisplayName("Should list all active plans")
    void shouldListActivePlans() {
        SubscriptionPlan plan = new SubscriptionPlan(
                PlanId.generate(),
                new StripePriceId("price_123"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(planRepository.findAllActive()).thenReturn(List.of(plan));

        List<SubscriptionPlan> result = queryService.handle(new ListActivePlansQuery());

        assertThat(result).hasSize(1);
        verify(planRepository).findAllActive();
    }

    @Test
    @DisplayName("Should find plan by Stripe price ID")
    void shouldFindByStripePriceId() {
        StripePriceId priceId = new StripePriceId("price_max_123");
        SubscriptionPlan plan = new SubscriptionPlan(
                PlanId.generate(),
                priceId,
                "Max Plan",
                PlanTier.MAX,
                PlanPricing.of(Money.of(new BigDecimal("199.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.maxPlanPreset(),
                true
        );

        when(planRepository.findByStripePriceId(priceId)).thenReturn(Optional.of(plan));

        Optional<SubscriptionPlan> result = queryService.findByStripePriceId(priceId);

        assertThat(result).isPresent();
        assertThat(result.get().tier()).isEqualTo(PlanTier.MAX);
    }
}
