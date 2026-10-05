package com.andeva.atelier.platform.inventory.interfaces.rest.assemblers;

import com.andeva.atelier.platform.inventory.domain.model.aggregates.InventoryItem;
import com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses.StockAlertResource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class StockAlertResourceAssembler {

    private StockAlertResourceAssembler() {
    }

    public static StockAlertResource toResource(InventoryItem item) {
        if (item == null) {
            return null;
        }

        BigDecimal deficit = item.getMinimumStock().value().subtract(item.getTotalStock().value());
        if (deficit.compareTo(BigDecimal.ZERO) < 0) {
            deficit = BigDecimal.ZERO;
        }

        return new StockAlertResource(
                item.getId().value(),
                item.getName(),
                item.getSku().value(),
                item.getCategory(),
                item.getTotalStock().value(),
                item.getMinimumStock().value(),
                deficit,
                Instant.now()
        );
    }

    public static List<StockAlertResource> toResourceList(List<InventoryItem> items) {
        if (items == null) {
            return List.of();
        }
        return items.stream().map(StockAlertResourceAssembler::toResource).toList();
    }
}
