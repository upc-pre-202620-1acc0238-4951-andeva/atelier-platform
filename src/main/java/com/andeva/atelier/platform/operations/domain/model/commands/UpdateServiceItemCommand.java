package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;

import java.math.BigDecimal;

public record UpdateServiceItemCommand(
        ServiceId serviceId,
        String name,
        BigDecimal basePrice,
        String currency,
        int estimatedMinutes
) {
}
