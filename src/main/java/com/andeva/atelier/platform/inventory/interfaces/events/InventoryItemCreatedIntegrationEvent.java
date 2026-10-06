package com.andeva.atelier.platform.inventory.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record InventoryItemCreatedIntegrationEvent(
        UUID itemId,
        UUID tenantId,
        String sku,
        String name,
        Instant occurredOn
) {
}
