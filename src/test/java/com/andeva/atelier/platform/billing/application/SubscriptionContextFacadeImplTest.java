package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.acl.SubscriptionContextFacadeImpl;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanFeatureId;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.*;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.FeatureEntitlementDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantSubscriptionStatusDto;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link SubscriptionContextFacadeImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionContext Facade Tests")
class SubscriptionContextFacadeImplTest {

    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionPlanRepository planRepository;

    private SubscriptionContextFacadeImpl facade;

    @BeforeEach
    void setUp() {
        facade = new SubscriptionContextFacadeImpl(subscriptionRepository, planRepository);
    }

    @Test
    @DisplayName("Should return true when tenant has an active subscription")
    void shouldReturnActiveSubscriptionStatus() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        boolean active = facade.isTenantSubscriptionActive(tenantUuid);

        assertThat(active).isTrue();

        // Check that subsequent invocation uses cache and does not hit repository again
        boolean cachedActive = facade.isTenantSubscriptionActive(tenantUuid);
        assertThat(cachedActive).isTrue();
        verify(subscriptionRepository, times(1)).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should evaluate branch addition quota accurately")
    void shouldEvaluateBranchAdditionQuota() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        // Pro preset has maxBranches = 2
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThat(facade.canAddBranch(tenantUuid, 1)).isTrue();
        assertThat(facade.canAddBranch(tenantUuid, 2)).isFalse();
    }

    @Test
    @DisplayName("Should evaluate staff member addition quota accurately")
    void shouldEvaluateStaffAdditionQuota() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        // Pro preset has maxActiveStaff = 10
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThat(facade.canAddStaffMember(tenantUuid, 9)).isTrue();
        assertThat(facade.canAddStaffMember(tenantUuid, 10)).isFalse();
    }

    @Test
    @DisplayName("Should evaluate work order creation quota accurately")
    void shouldEvaluateWorkOrderQuota() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        // Go preset has maxMonthlyWorkOrders = 50
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_go"),
                "Go Plan",
                PlanTier.GO,
                PlanPricing.of(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset(),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThat(facade.canCreateWorkOrder(tenantUuid, 49)).isTrue();
        assertThat(facade.canCreateWorkOrder(tenantUuid, 50)).isFalse();
    }

    @Test
    @DisplayName("Should evaluate feature permissions and modular feature toggles")
    void shouldEvaluateFeaturePermissions() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        PlanFeature customFeature = PlanFeature.create(
                planId,
                "custom_reporting",
                "Custom report generator",
                true
        );

        // Max plan preset has iotTelemetryEnabled = true, aiDiagnosticsEnabled = true, companyRegistrationAllowed = true, multiWarehouseAllowed = true
        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_max"),
                "Max Plan",
                PlanTier.MAX,
                PlanPricing.of(Money.of(new BigDecimal("199.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.maxPlanPreset(),
                List.of(customFeature),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        assertThat(facade.isFeatureAllowed(tenantUuid, "iot_telemetry")).isTrue();
        assertThat(facade.isFeatureAllowed(tenantUuid, "ai_diagnostics")).isTrue();
        assertThat(facade.isFeatureAllowed(tenantUuid, "multi_warehouse")).isTrue();
        assertThat(facade.isFeatureAllowed(tenantUuid, "company_registration")).isTrue();
        assertThat(facade.isFeatureAllowed(tenantUuid, "custom_reporting")).isTrue();
        assertThat(facade.isFeatureAllowed(tenantUuid, "non_existent_feature")).isFalse();
    }

    @Test
    @DisplayName("Should return full quota limits DTO and subscription status DTO")
    void shouldReturnQuotaLimitsAndStatusDtos() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        TenantQuotaLimitsDto limitsDto = facade.getTenantQuotaLimits(tenantUuid);
        assertThat(limitsDto.maxBranches()).isEqualTo(2);

        TenantSubscriptionStatusDto statusDto = facade.getTenantSubscriptionStatus(tenantUuid);
        assertThat(statusDto.planName()).isEqualTo("Pro Plan");
        assertThat(statusDto.tier()).isEqualTo("PRO");
        assertThat(statusDto.isActive()).isTrue();

        FeatureEntitlementDto entitlement = facade.checkFeatureEntitlement(tenantUuid, "branches");
        assertThat(entitlement.isEntitled()).isFalse(); // "branches" is not a feature toggle key
        assertThat(entitlement.maximumLimit()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should reload from repository after cache eviction")
    void shouldReloadFromRepositoryAfterEviction() {
        UUID tenantUuid = UUID.randomUUID();
        TenantId tenantId = new TenantId(tenantUuid);
        PlanId planId = PlanId.generate();

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                planId,
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        SubscriptionPlan plan = new SubscriptionPlan(
                planId,
                new StripePriceId("price_pro"),
                "Pro Plan",
                PlanTier.PRO,
                PlanPricing.of(Money.of(new BigDecimal("99.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset(),
                true
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));

        facade.isTenantSubscriptionActive(tenantUuid);
        facade.evictCache(tenantUuid);
        facade.isTenantSubscriptionActive(tenantUuid);

        verify(subscriptionRepository, times(2)).findByTenantId(tenantId);
    }
}
