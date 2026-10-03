package com.andeva.atelier.platform.billing.application;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.internal.queryservices.TenantSubscriptionQueryServiceImpl;
import com.andeva.atelier.platform.billing.application.queryservices.TenantSubscriptionQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.queries.CheckTenantQuotaQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.GetTenantSubscriptionQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.IsTenantSubscriptionActiveQuery;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for {@link TenantSubscriptionQueryServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantSubscription Query Service Tests")
class TenantSubscriptionQueryServiceTest {

    @Mock
    private TenantSubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionPlanRepository planRepository;
    @Mock
    private BillingCachePort billingCachePort;

    private TenantSubscriptionQueryService queryService;

    @BeforeEach
    void setUp() {
        queryService = new TenantSubscriptionQueryServiceImpl(
                subscriptionRepository,
                planRepository,
                billingCachePort
        );
    }

    @Test
    @DisplayName("Should return true immediately if subscription status is present in cache")
    void shouldReturnCachedActiveStatus() {
        TenantId tenantId = TenantId.generate();
        when(billingCachePort.isSubscriptionActive(tenantId)).thenReturn(Optional.of(true));

        boolean active = queryService.isSubscriptionActive(tenantId);

        assertThat(active).isTrue();
        verifyNoInteractions(subscriptionRepository);
    }

    @Test
    @DisplayName("Should query repository and cache active status when cache miss occurs")
    void shouldQueryRepositoryOnCacheMiss() {
        TenantId tenantId = TenantId.generate();
        when(billingCachePort.isSubscriptionActive(tenantId)).thenReturn(Optional.empty());

        TenantSubscription subscription = TenantSubscription.activate(
                tenantId,
                PlanId.generate(),
                new StripeCustomerId("cus_123"),
                new StripeSubscriptionId("sub_123"),
                SubscriptionPeriod.of(Instant.now(), Instant.now().plus(Duration.ofDays(30)))
        );

        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.of(subscription));

        boolean active = queryService.handle(new IsTenantSubscriptionActiveQuery(tenantId));

        assertThat(active).isTrue();
        verify(billingCachePort).cacheSubscriptionActive(tenantId, true);
    }

    @Test
    @DisplayName("Should return effective plan quotas for active subscriber")
    void shouldReturnPlanQuotasForActiveSubscriber() {
        TenantId tenantId = TenantId.generate();
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

        TenantQuotaLimits limits = queryService.handle(new CheckTenantQuotaQuery(tenantId));

        assertThat(limits.maxBranches()).isEqualTo(2);
        assertThat(limits.maxActiveStaff()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should return default limits when subscription not found")
    void shouldReturnDefaultQuotasWhenNotFound() {
        TenantId tenantId = TenantId.generate();
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(Optional.empty());

        TenantQuotaLimits limits = queryService.handle(new CheckTenantQuotaQuery(tenantId));

        assertThat(limits).isEqualTo(TenantQuotaLimits.defaults());
    }

    @Test
    @DisplayName("Should evict subscription cache")
    void shouldEvictSubscriptionCache() {
        TenantId tenantId = TenantId.generate();
        queryService.evictSubscriptionCache(tenantId);
        verify(billingCachePort).evict(tenantId);
    }
}
