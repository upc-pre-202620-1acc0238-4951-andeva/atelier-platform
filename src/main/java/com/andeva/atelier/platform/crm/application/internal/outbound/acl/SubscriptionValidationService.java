package com.andeva.atelier.platform.crm.application.internal.outbound.acl;

import java.util.UUID;

/**
 * Outbound ACL port for querying SaaS plan quotas and validation limits in Billing.
 *
 * @author Adiel Sanchez Santin
 */
public interface SubscriptionValidationService {

    boolean validateCustomerQuota(UUID tenantId);

    boolean validateVehicleQuota(UUID tenantId);
}
