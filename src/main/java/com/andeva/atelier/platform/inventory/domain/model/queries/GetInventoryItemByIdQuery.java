package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.ids.InventoryItemId;

import java.util.Objects;

public record GetInventoryItemByIdQuery(
        InventoryItemId itemId
) {
    public GetInventoryItemByIdQuery {
        Objects.requireNonNull(itemId, "itemId cannot be null");
    }
}
