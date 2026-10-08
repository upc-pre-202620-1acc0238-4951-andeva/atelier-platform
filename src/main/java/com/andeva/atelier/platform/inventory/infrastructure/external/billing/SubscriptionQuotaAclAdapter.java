package com.andeva.atelier.platform.inventory.infrastructure.external.billing;

import com.andeva.atelier.platform.billing.interfaces.acl.SubscriptionContextFacade;
import com.andeva.atelier.platform.inventory.application.internal.outbound.acl.SubscriptionQuotaAclGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Outbound ACL adapter integrating with SaaS Billing & Subscriptions SubscriptionContextFacade.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionQuotaAclAdapter implements SubscriptionQuotaAclGateway {

    private final SubscriptionContextFacade subscriptionContextFacade;

    @Autowired
    public SubscriptionQuotaAclAdapter(@Autowired(required = false) SubscriptionContextFacade subscriptionContextFacade) {
        this.subscriptionContextFacade = subscriptionContextFacade;
    }

    public SubscriptionQuotaAclAdapter() {
        this(null);
    }

    @Override
    public boolean validateMultiWarehouseTransferAllowed(UUID tenantId) {
        if (tenantId == null) {
            return false;
        }
        if (subscriptionContextFacade == null) {
            return true; // Resilient fallback
        }
        try {
            return subscriptionContextFacade.isTenantSubscriptionActive(tenantId);
        } catch (Exception e) {
            return true; // Fallback on communication error
        }
    }
}
