package com.andeva.atelier.platform.billing.application.internal.eventhandlers;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.domain.exceptions.DuplicateActiveSubscriptionException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.iam.interfaces.events.TenantCreatedIntegrationEvent;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Integration Event Listener reacting to tenant creation events from IAM & Tenancy
 * by automatically provisioning a 14-day Free Trial subscription.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TenantLifecycleIntegrationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TenantLifecycleIntegrationEventHandler.class);
    private static final int DEFAULT_FREE_TRIAL_DAYS = 14;

    private final TenantSubscriptionCommandService tenantSubscriptionCommandService;
    private final SubscriptionPlanRepository planRepository;

    public TenantLifecycleIntegrationEventHandler(
            TenantSubscriptionCommandService tenantSubscriptionCommandService,
            SubscriptionPlanRepository planRepository
    ) {
        this.tenantSubscriptionCommandService = Objects.requireNonNull(tenantSubscriptionCommandService, "TenantSubscriptionCommandService cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
    }

    @EventListener
    public void on(TenantCreatedIntegrationEvent event) {
        Objects.requireNonNull(event, "TenantCreatedIntegrationEvent cannot be null");
        TenantId tenantId = new TenantId(event.tenantId());
        log.info("Processing TenantCreatedIntegrationEvent for new tenant: {} ({})", tenantId.value(), event.name());

        List<SubscriptionPlan> activePlans = planRepository.findAllActive();
        Optional<SubscriptionPlan> defaultPlanOpt = activePlans.stream()
                .filter(p -> p.tier() == PlanTier.GO)
                .findFirst()
                .or(() -> activePlans.stream().findFirst());

        if (defaultPlanOpt.isEmpty()) {
            log.warn("Cannot automatically provision free trial for tenant {}: No active subscription plans found in catalog", tenantId.value());
            return;
        }

        SubscriptionPlan defaultPlan = defaultPlanOpt.get();

        try {
            tenantSubscriptionCommandService.handleStartTrial(tenantId, defaultPlan.id(), DEFAULT_FREE_TRIAL_DAYS);
            log.info("Successfully provisioned {}-day trial on plan '{}' for tenant {}",
                    DEFAULT_FREE_TRIAL_DAYS, defaultPlan.name(), tenantId.value());
        } catch (DuplicateActiveSubscriptionException e) {
            log.warn("Trial provision skipped for tenant {}: already has an active subscription", tenantId.value());
        } catch (Exception e) {
            log.error("Failed to provision free trial for tenant {}: {}", tenantId.value(), e.getMessage(), e);
        }
    }
}
