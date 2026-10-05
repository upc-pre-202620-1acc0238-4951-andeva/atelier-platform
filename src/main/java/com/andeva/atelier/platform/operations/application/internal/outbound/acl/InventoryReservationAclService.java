package com.andeva.atelier.platform.operations.application.internal.outbound.acl;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface InventoryReservationAclService {

    boolean checkItemStockAvailability(UUID inventoryItemId, BigDecimal requiredQuantity);

    Optional<InventoryItemSummaryDto> fetchItemDetails(UUID inventoryItemId);
}
