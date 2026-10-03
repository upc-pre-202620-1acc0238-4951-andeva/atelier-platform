package com.andeva.atelier.platform.billing.application.queryservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.queries.CheckTenantQuotaQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.GetTenantSubscriptionQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.IsTenantSubscriptionActiveQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Optional;

/**
 * Application Query Service contract for querying tenant subscriptions, quota limits, and active authorization.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantSubscriptionQueryService {

    /**
     * Retrieves the current subscription contract of a workshop tenant.
     *
     * @param query GetTenantSubscriptionQuery
     * @return Optional containing TenantSubscription if found
     */
    Optional<TenantSubscription> handle(GetTenantSubscriptionQuery query);

    /**
     * Determines whether the tenant's subscription is currently active or inside the statutory grace period.
     *
     * @param query IsTenantSubscriptionActiveQuery
     * @return true if authorized to operate on the platform
     */
    boolean handle(IsTenantSubscriptionActiveQuery query);

    /**
     * Inspects the effective operational quota limits for a tenant based on their active plan.
     *
     * @param query CheckTenantQuotaQuery
     * @return TenantQuotaLimits
     */
    TenantQuotaLimits handle(CheckTenantQuotaQuery query);

    /**
     * Fast cached evaluation of active status for a tenant.
     *
     * @param tenantId workshop tenant identifier
     * @return true if subscription is active
     */
    boolean isSubscriptionActive(TenantId tenantId);

    /**
     * Evicts the cached subscription authorization entry for a workshop tenant.
     *
     * @param tenantId workshop tenant identifier
     */
    void evictSubscriptionCache(TenantId tenantId);
}
