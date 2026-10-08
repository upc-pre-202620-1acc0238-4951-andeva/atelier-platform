package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound Anti-Corruption Layer service for stock reservations and inventory queries.
 *
 * @author Joel Huamani Estefanero
 */
public interface InventoryReservationAclService {

    boolean checkItemStockAvailability(UUID inventoryItemId, BigDecimal requiredQuantity);

    Optional<InventoryItemSummaryDto> fetchItemDetails(UUID inventoryItemId);

    default boolean checkItemStockAvailability(UUID tenantId, UUID inventoryItemId, BigDecimal requiredQuantity) {
        return checkItemStockAvailability(inventoryItemId, requiredQuantity);
    }

    default Optional<InventoryItemSummaryDto> fetchItemDetails(UUID tenantId, UUID inventoryItemId) {
        return fetchItemDetails(inventoryItemId);
    }
}
