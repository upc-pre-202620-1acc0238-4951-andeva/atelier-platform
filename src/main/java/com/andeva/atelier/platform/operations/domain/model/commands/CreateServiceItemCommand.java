package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.math.BigDecimal;

public record CreateServiceItemCommand(
        TenantId tenantId,
        String name,
        BigDecimal basePrice,
        String currency,
        int estimatedMinutes
) {
}
