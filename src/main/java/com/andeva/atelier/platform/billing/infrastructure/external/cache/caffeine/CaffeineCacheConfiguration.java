package com.andeva.atelier.platform.billing.infrastructure.external.cache.caffeine;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache configuration for ultra-low latency (< 0.05ms) in-memory subscription lookups.
 *
 * @author Joel Huamani Estefanero
 */
@Configuration
public class CaffeineCacheConfiguration {

    public static final String CACHE_TENANT_SUBSCRIPTION_STATUS = "tenantSubscriptionStatus";
    public static final String CACHE_ACTIVE_PLANS = "activePlans";

    @Bean
    public CacheManager billingCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CACHE_TENANT_SUBSCRIPTION_STATUS,
                CACHE_ACTIVE_PLANS
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(10_000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
