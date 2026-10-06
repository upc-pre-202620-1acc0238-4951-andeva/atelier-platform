package com.andeva.atelier.platform.inventory.interfaces.events;

import java.time.Instant;
import java.util.UUID;

public record SupplierRegisteredIntegrationEvent(
        UUID supplierId,
        UUID tenantId,
        String businessName,
        String taxId,
        Instant occurredOn
) {
}
