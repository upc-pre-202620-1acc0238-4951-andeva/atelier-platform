package com.andeva.atelier.platform.billing.application.internal.outbound.acl;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Optional;

/**
 * Outbound port for high-performance In-Memory cache operations (Caffeine)
 * regarding workshop tenant subscription authorization.
 *
 * @author Joel Huamani Estefanero
 */
public interface BillingCachePort {

    /**
     * Checks cached subscription active status for the tenant.
     *
     * @param tenantId workshop tenant identifier
     * @return Optional containing the cached boolean state, or empty if not cached
     */
    Optional<Boolean> isSubscriptionActive(TenantId tenantId);

    /**
     * Stores the evaluated subscription active status in cache.
     *
     * @param tenantId workshop tenant identifier
     * @param active   boolean authorization state
     */
    void cacheSubscriptionActive(TenantId tenantId, boolean active);

    /**
     * Evicts the cached subscription status for a workshop tenant.
     *
     * @param tenantId workshop tenant identifier
     */
    void evict(TenantId tenantId);
}
