package com.andeva.atelier.platform.inventory.interfaces.rest.resources.responses;

import java.util.UUID;

public record SupplierResource(
        UUID id,
        UUID tenantId,
        String businessName,
        String taxId,
        String contactName,
        String phone,
        String email,
        String address,
        boolean active
) {
}
