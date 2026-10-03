package com.andeva.atelier.platform.billing.application.acl;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.FeatureEntitlementDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantSubscriptionStatusDto;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Inbound Open Host Service (OHS) facade implementation of SubscriptionContextFacade.
 * Serves downstream bounded contexts (IAM, MRO, HR, IoT) with sub-millisecond cached policy lookups.
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class SubscriptionContextFacadeImpl implements SubscriptionContextFacade {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionContextFacadeImpl.class);

    private final TenantSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final Cache<UUID, CachedTenantSubscriptionPolicy> policyCache;

    public SubscriptionContextFacadeImpl(
            TenantSubscriptionRepository subscriptionRepository,
            SubscriptionPlanRepository planRepository
    ) {
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository, "TenantSubscriptionRepository cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
        this.policyCache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterWrite(Duration.ofMinutes(30))
                .recordStats()
                .build();
    }

    @Override
    public boolean isTenantSubscriptionActive(UUID tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return getOrLoadPolicy(tenantId).isActive();
    }

    @Override
    public TenantQuotaLimitsDto getTenantQuotaLimits(UUID tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        return getOrLoadPolicy(tenantId).quotas();
    }

    @Override
    public TenantSubscriptionStatusDto getTenantSubscriptionStatus(UUID tenantId) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        CachedTenantSubscriptionPolicy policy = getOrLoadPolicy(tenantId);
        return new TenantSubscriptionStatusDto(
                tenantId,
                policy.planName(),
                policy.tier(),
                policy.status(),
                policy.isActive(),
                policy.periodEnd(),
                policy.cancelAtPeriodEnd()
        );
    }

    @Override
    public boolean canAddBranch(UUID tenantId, int currentBranchCount) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        CachedTenantSubscriptionPolicy policy = getOrLoadPolicy(tenantId);
        if (!policy.isActive()) {
            return false;
        }
        int max = policy.quotas().maxBranches();
        return max == -1 || currentBranchCount < max;
    }

    @Override
    public boolean canAddStaffMember(UUID tenantId, int currentStaffCount) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        CachedTenantSubscriptionPolicy policy = getOrLoadPolicy(tenantId);
        if (!policy.isActive()) {
            return false;
        }
        int max = policy.quotas().maxActiveStaff();
        return max == -1 || currentStaffCount < max;
    }

    @Override
    public boolean canCreateWorkOrder(UUID tenantId, int currentMonthlyWorkOrders) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        CachedTenantSubscriptionPolicy policy = getOrLoadPolicy(tenantId);
        if (!policy.isActive()) {
            return false;
        }
        int max = policy.quotas().maxMonthlyWorkOrders();
        return max == -1 || currentMonthlyWorkOrders < max;
    }

    @Override
    public boolean isFeatureAllowed(UUID tenantId, String featureKey) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        if (featureKey == null || featureKey.isBlank()) {
            return false;
        }
        CachedTenantSubscriptionPolicy policy = getOrLoadPolicy(tenantId);
        if (!policy.isActive()) {
            return false;
        }

        String normalizedKey = featureKey.trim().toLowerCase();
        if ("iot_telemetry".equals(normalizedKey) || "obd2".equals(normalizedKey)) {
            return policy.quotas().iotTelemetryEnabled();
        }
        if ("ai_diagnostics".equals(normalizedKey) || "ai_reports".equals(normalizedKey)) {
            return policy.quotas().aiDiagnosticsEnabled();
        }
        if ("multi_warehouse".equals(normalizedKey)) {
            return policy.quotas().multiWarehouseAllowed();
        }
        if ("company_registration".equals(normalizedKey)) {
            return policy.quotas().companyRegistrationAllowed();
        }

        return policy.allowedFeatures().stream()
                .anyMatch(f -> f.equalsIgnoreCase(normalizedKey));
    }

    @Override
    public FeatureEntitlementDto checkFeatureEntitlement(UUID tenantId, String featureKey) {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        boolean allowed = isFeatureAllowed(tenantId, featureKey);
        CachedTenantSubscriptionPolicy policy = getOrLoadPolicy(tenantId);
        int maxLimit = 1;
        if (featureKey != null) {
            String k = featureKey.trim().toLowerCase();
            if ("branches".equals(k)) maxLimit = policy.quotas().maxBranches();
            else if ("staff".equals(k)) maxLimit = policy.quotas().maxActiveStaff();
            else if ("work_orders".equals(k)) maxLimit = policy.quotas().maxMonthlyWorkOrders();
            else if ("ai_reports".equals(k)) maxLimit = policy.quotas().maxMonthlyAiReports();
            else if ("obd2".equals(k) || "iot_telemetry".equals(k)) maxLimit = policy.quotas().maxActiveObd2Devices();
        }
        return new FeatureEntitlementDto(tenantId, featureKey, allowed, 0, maxLimit);
    }

    /**
     * Evicts the cached policy for the specified tenant identifier.
     *
     * @param tenantId workshop tenant identifier
     */
    public void evictCache(UUID tenantId) {
        if (tenantId != null) {
            policyCache.invalidate(tenantId);
            log.debug("RAM policy cache invalidated for tenant {}", tenantId);
        }
    }

    @org.springframework.context.event.EventListener
    public void on(com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionActivatedEvent event) {
        evictCache(event.tenantId().value());
    }

    @org.springframework.context.event.EventListener
    public void on(com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionRenewedEvent event) {
        evictCache(event.tenantId().value());
    }

    @org.springframework.context.event.EventListener
    public void on(com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionPastDueEvent event) {
        evictCache(event.tenantId().value());
    }

    @org.springframework.context.event.EventListener
    public void on(com.andeva.atelier.platform.billing.domain.model.events.TenantSubscriptionCanceledEvent event) {
        evictCache(event.tenantId().value());
    }

    @org.springframework.context.event.EventListener
    public void on(com.andeva.atelier.platform.billing.domain.model.events.TenantPlanChangedEvent event) {
        evictCache(event.tenantId().value());
    }

    private CachedTenantSubscriptionPolicy getOrLoadPolicy(UUID tenantId) {
        return policyCache.get(tenantId, id -> {
            log.debug("Cache miss for tenant {}. Loading policy from repository...", id);
            return subscriptionRepository.findByTenantId(new TenantId(id))
                    .flatMap(sub -> planRepository.findById(sub.planId())
                            .map(plan -> {
                                TenantQuotaLimitsDto quotaDto = new TenantQuotaLimitsDto(
                                        plan.quotaLimits().maxBranches(),
                                        plan.quotaLimits().maxActiveStaff(),
                                        plan.quotaLimits().maxActiveObd2Devices(),
                                        plan.quotaLimits().maxPhotosPerWorkOrder(),
                                        plan.quotaLimits().maxMonthlyAiReports(),
                                        plan.quotaLimits().companyRegistrationAllowed(),
                                        plan.quotaLimits().multiWarehouseAllowed(),
                                        plan.quotaLimits().marketplaceListed(),
                                        plan.quotaLimits().maxMonthlyWorkOrders(),
                                        plan.quotaLimits().iotTelemetryEnabled(),
                                        plan.quotaLimits().aiDiagnosticsEnabled()
                                );

                                Set<String> features = plan.features().stream()
                                        .filter(com.andeva.atelier.platform.billing.domain.model.entities.PlanFeature::isEnabled)
                                        .map(f -> f.featureKey().toLowerCase())
                                        .collect(Collectors.toUnmodifiableSet());

                                return new CachedTenantSubscriptionPolicy(
                                        sub.isAccessGranted(),
                                        plan.name(),
                                        plan.tier().name(),
                                        sub.status().name(),
                                        sub.currentPeriod().endDate(),
                                        sub.isCancelAtPeriodEnd(),
                                        quotaDto,
                                        features
                                );
                            }))
                    .orElseGet(CachedTenantSubscriptionPolicy::inactiveDefault);
        });
    }

    private record CachedTenantSubscriptionPolicy(
            boolean isActive,
            String planName,
            String tier,
            String status,
            Instant periodEnd,
            boolean cancelAtPeriodEnd,
            TenantQuotaLimitsDto quotas,
            Set<String> allowedFeatures
    ) {
        public static CachedTenantSubscriptionPolicy inactiveDefault() {
            return new CachedTenantSubscriptionPolicy(
                    false,
                    "Sin Plan",
                    "NONE",
                    "INACTIVE",
                    Instant.EPOCH,
                    false,
                    new TenantQuotaLimitsDto(0, 0, 0, 0, 0, false, false, false, 0, false, false),
                    Set.of()
            );
        }
    }
}
