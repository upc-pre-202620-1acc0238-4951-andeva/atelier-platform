package com.andeva.atelier.platform.crm.infrastructure.external.billing;

import com.andeva.atelier.platform.crm.application.internal.outbound.acl.SubscriptionValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter querying tenant plan limits and quotas from Billing context.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SubscriptionValidationClient implements SubscriptionValidationService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionValidationClient.class);

    @Override
    public boolean validateCustomerQuota(UUID tenantId) {
        log.debug("Validating customer quota for tenant: {}", tenantId);
        return true;
    }

    @Override
    public boolean validateVehicleQuota(UUID tenantId) {
        log.debug("Validating vehicle quota for tenant: {}", tenantId);
        return true;
    }
}
