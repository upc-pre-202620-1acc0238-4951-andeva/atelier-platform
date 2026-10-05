package com.andeva.atelier.platform.operations.infrastructure.external.acl.inventory;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryItemSummaryDto;
import com.andeva.atelier.platform.operations.application.internal.outbound.acl.InventoryReservationAclService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
public class InventoryReservationAclAdapter implements InventoryReservationAclService {

    @Override
    public boolean checkItemStockAvailability(UUID inventoryItemId, BigDecimal requiredQuantity) {
        return inventoryItemId != null && requiredQuantity != null && requiredQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public Optional<InventoryItemSummaryDto> fetchItemDetails(UUID inventoryItemId) {
        if (inventoryItemId == null) return Optional.empty();
        return Optional.of(new InventoryItemSummaryDto(
                inventoryItemId,
                "OIL-5W30-SYN",
                "Aceite Sintético 5W30",
                new BigDecimal("50.00"),
                new BigDecimal("5.00"),
                "LITROS",
                "PEN"
        ));
    }
}
