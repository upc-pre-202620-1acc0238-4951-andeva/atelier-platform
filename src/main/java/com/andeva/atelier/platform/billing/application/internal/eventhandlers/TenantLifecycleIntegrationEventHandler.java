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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

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
    private final TransactionOperations trialTransaction;

    @Autowired
    public TenantLifecycleIntegrationEventHandler(
            TenantSubscriptionCommandService tenantSubscriptionCommandService,
            SubscriptionPlanRepository planRepository,
            PlatformTransactionManager transactionManager
    ) {
        this(tenantSubscriptionCommandService, planRepository, requiresNewTransaction(transactionManager));
    }

    public TenantLifecycleIntegrationEventHandler(
            TenantSubscriptionCommandService tenantSubscriptionCommandService,
            SubscriptionPlanRepository planRepository,
            TransactionOperations trialTransaction
    ) {
        this.tenantSubscriptionCommandService = Objects.requireNonNull(tenantSubscriptionCommandService, "TenantSubscriptionCommandService cannot be null");
        this.planRepository = Objects.requireNonNull(planRepository, "SubscriptionPlanRepository cannot be null");
        this.trialTransaction = Objects.requireNonNull(trialTransaction, "TransactionOperations cannot be null");
    }

    // The event arrives after the IAM sign-up commit: the trial needs a transaction of its own to persist,
    // and a failure there (e.g. Stripe unavailable) must roll back only the trial, never the registered tenant
    private static TransactionOperations requiresNewTransaction(PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(
                Objects.requireNonNull(transactionManager, "PlatformTransactionManager cannot be null"));
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return template;
    }

    @EventListener
    public void on(TenantCreatedIntegrationEvent event) {
        Objects.requireNonNull(event, "TenantCreatedIntegrationEvent cannot be null");
        TenantId tenantId = new TenantId(event.tenantId());
        log.info("Processing TenantCreatedIntegrationEvent for new tenant: {} ({})", tenantId.value(), event.name());

        try {
            trialTransaction.executeWithoutResult(status -> startTrial(tenantId));
        } catch (DuplicateActiveSubscriptionException e) {
            log.warn("Trial provision skipped for tenant {}: already has an active subscription", tenantId.value());
        } catch (Exception e) {
            log.error("Failed to provision free trial for tenant {}: {}", tenantId.value(), e.getMessage(), e);
        }
    }

    private void startTrial(TenantId tenantId) {
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
        tenantSubscriptionCommandService.handleStartTrial(tenantId, defaultPlan.id(), DEFAULT_FREE_TRIAL_DAYS);
        log.info("Successfully provisioned {}-day trial on plan '{}' for tenant {}",
                DEFAULT_FREE_TRIAL_DAYS, defaultPlan.name(), tenantId.value());
    }
}
