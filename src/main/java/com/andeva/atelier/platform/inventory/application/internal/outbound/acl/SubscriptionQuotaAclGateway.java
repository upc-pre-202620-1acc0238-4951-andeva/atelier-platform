package com.andeva.atelier.platform.inventory.application.internal.outbound.acl;

import java.util.UUID;

/**
 * Outbound ACL gateway to validate SaaS subscription quota limits for inventory operations.
 *
 * @author Joel Huamani Estefanero
 */
public interface SubscriptionQuotaAclGateway {
    boolean validateMultiWarehouseTransferAllowed(UUID tenantId);
}
