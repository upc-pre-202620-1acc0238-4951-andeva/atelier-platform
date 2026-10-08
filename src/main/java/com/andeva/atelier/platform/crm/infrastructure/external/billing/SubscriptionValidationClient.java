package com.andeva.atelier.platform.crm.infrastructure.external.billing;

import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.crm.application.internal.outbound.acl.SubscriptionValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter querying tenant plan limits and quotas from Billing context.
 * Connects directly to Billing OHS facade {@link SubscriptionContextFacade} with resilient fallbacks.
 *
 * @author Joel Huamani Estefanero
 * @author Adiel Sanchez Santin
 */
@Component
public class SubscriptionValidationClient implements SubscriptionValidationService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionValidationClient.class);

    private final SubscriptionContextFacade subscriptionContextFacade;

    @Autowired
    public SubscriptionValidationClient(@Autowired(required = false) SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    public SubscriptionValidationClient() {
        this(null);
    }

    @Override
    public boolean validateCustomerQuota(UUID tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (subscriptionContextFacade == null) {
            log.debug("SubscriptionContextFacade not present; allowing customer quota by default for tenant: {}", tenantId);
            return true;
        }
        try {
            boolean active = subscriptionContextFacade.isTenantSubscriptionActive(tenantId);
            if (!active) {
                log.warn("Customer quota validation rejected: tenant {} has no active subscription", tenantId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Failed to query subscription status for customer quota on tenant {}: {}", tenantId, e.getMessage());
            return true;
        }
    }

    @Override
    public boolean validateVehicleQuota(UUID tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (subscriptionContextFacade == null) {
            log.debug("SubscriptionContextFacade not present; allowing vehicle quota by default for tenant: {}", tenantId);
            return true;
        }
        try {
            boolean active = subscriptionContextFacade.isTenantSubscriptionActive(tenantId);
            if (!active) {
                log.warn("Vehicle quota validation rejected: tenant {} has no active subscription", tenantId);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Failed to query subscription status for vehicle quota on tenant {}: {}", tenantId, e.getMessage());
            return true;
        }
    }
}

