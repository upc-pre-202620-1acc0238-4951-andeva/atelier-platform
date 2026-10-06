package com.andeva.atelier.platform.inventory.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PartSummaryDto(
        UUID id,
        String name,
        String sku,
        String category,
        BigDecimal basePrice,
        BigDecimal totalStock,
        String status
) {
}
