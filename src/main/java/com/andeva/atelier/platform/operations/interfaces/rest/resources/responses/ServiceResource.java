package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResource(
        UUID id,
        UUID tenantId,
        String name,
        BigDecimal basePrice,
        String currency,
        int estimatedMinutes
) {}
