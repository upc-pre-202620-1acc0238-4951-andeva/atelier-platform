package com.andeva.atelier.platform.billing.application.internal.queryservices;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.BillingCachePort;
import com.andeva.atelier.platform.billing.application.queryservices.TenantSubscriptionQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.queries.CheckTenantQuotaQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.GetTenantSubscriptionQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.IsTenantSubscriptionActiveQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of TenantSubscriptionQueryService retrieving tenant subscription agreements,
 * effective quota limits, and offering cached sub-millisecond status lookups.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class TenantSubscriptionQueryServiceImpl implements TenantSubscriptionQueryService {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final BillingCachePort billingCachePort;

    public TenantSubscriptionQueryServiceImpl(
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository,
            BillingCachePort billingCachePort
    ) {
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "TenantSubscriptionRepository cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
        this.billingCachePort = Objects.requireNonNull(billingCachePort, "BillingCachePort cannot be null");
    }

    @Override
    public Optional<TenantSubscription> handle(GetTenantSubscriptionQuery query) {
        Objects.requireNonNull(query, "GetTenantSubscriptionQuery cannot be null");
        return subscriptionRepository.findByTenantId(query.tenantId());
    }

    @Override
    public boolean handle(IsTenantSubscriptionActiveQuery query) {
        Objects.requireNonNull(query, "IsTenantSubscriptionActiveQuery cannot be null");
        return isSubscriptionActive(query.tenantId());
    }

    @Override
    public TenantQuotaLimits handle(CheckTenantQuotaQuery query) {
        Objects.requireNonNull(query, "CheckTenantQuotaQuery cannot be null");
        return subscriptionRepository.findByTenantId(query.tenantId())
                .filter(TenantSubscription::isAccessGranted)
                .flatMap(sub -> planRepository.findById(sub.planId()))
                .map(SubscriptionPlan::quotaLimits)
                .orElse(TenantQuotaLimits.defaults());
    }

    @Override
    @Cacheable(value = "tenantSubscriptionStatus", key = "#tenantId.value().toString()")
    public boolean isSubscriptionActive(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");

        Optional<Boolean> cached = billingCachePort.isSubscriptionActive(tenantId);
        if (cached.isPresent()) {
            return cached.get();
        }

        boolean active = subscriptionRepository.findByTenantId(tenantId)
                .map(TenantSubscription::isAccessGranted)
                .orElse(false);

        billingCachePort.cacheSubscriptionActive(tenantId, active);
        return active;
    }

    @Override
    @CacheEvict(value = "tenantSubscriptionStatus", key = "#tenantId.value().toString()")
    public void evictSubscriptionCache(TenantId tenantId) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        billingCachePort.evict(tenantId);
    }
}
