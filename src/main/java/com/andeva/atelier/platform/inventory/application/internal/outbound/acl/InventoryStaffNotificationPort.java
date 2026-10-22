package com.andeva.atelier.platform.inventory.application.internal.outbound.acl;

import java.util.Map;
import java.util.UUID;

/**
 * Outbound port for push notifications to the workshop staff in charge of the stock.
 */
public interface InventoryStaffNotificationPort {

    /**
     * Every active member of the tenant whose roles grant the permission.
     */
    void notifyPermissionHolders(UUID tenantId, String permission, String title, String body, Map<String, String> data);
}
