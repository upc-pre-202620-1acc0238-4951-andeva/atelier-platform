package com.andeva.atelier.platform.billing.application.internal.eventhandlers;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Gives the free trial to every workshop left without a subscription — e.g. one that signed up before the plan
 * catalog was loaded, when the sign-up trial found no plan to start. Runs once at start-up and is idempotent.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class TrialSubscriptionReconciler {

    private static final Logger log = LoggerFactory.getLogger(TrialSubscriptionReconciler.class);

    private final IamTenantValidationAclPort iamTenantValidationAclPort;
    private final TenantSubscriptionRepository subscriptionRepository;
    private final TenantLifecycleIntegrationEventHandler trialProvisioner;

    public TrialSubscriptionReconciler(IamTenantValidationAclPort iamTenantValidationAclPort,
                                       TenantSubscriptionRepository subscriptionRepository,
                                       TenantLifecycleIntegrationEventHandler trialProvisioner) {
        this.iamTenantValidationAclPort = Objects.requireNonNull(iamTenantValidationAclPort);
        this.subscriptionRepository = Objects.requireNonNull(subscriptionRepository);
        this.trialProvisioner = Objects.requireNonNull(trialProvisioner);
    }

    @Order(0)
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        try {
            int granted = reconcile();
            if (granted > 0) {
                log.info("Started the free trial of {} workshop(s) that had no subscription", granted);
            }
        } catch (RuntimeException e) {
            log.error("Trial reconciliation failed: {}", e.getMessage(), e);
        }
    }

    /**
     * @return number of trials started
     */
    public int reconcile() {
        int granted = 0;
        for (TenantId tenantId : iamTenantValidationAclPort.listTenantIds()) {
            if (subscriptionRepository.findByTenantId(tenantId).isEmpty() && trialProvisioner.provisionTrial(tenantId, null)) {
                granted++;
            }
        }
        return granted;
    }
}
