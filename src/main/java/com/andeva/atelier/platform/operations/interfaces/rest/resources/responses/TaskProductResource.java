package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record TaskProductResource(
        UUID id,
        UUID taskId,
        UUID productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String currency
) {}
