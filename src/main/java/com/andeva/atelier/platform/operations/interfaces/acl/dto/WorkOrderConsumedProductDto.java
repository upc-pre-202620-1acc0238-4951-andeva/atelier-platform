package com.andeva.atelier.platform.operations.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkOrderConsumedProductDto(
        UUID productId,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String currency
) {}
