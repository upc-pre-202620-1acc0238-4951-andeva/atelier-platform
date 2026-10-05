package com.andeva.atelier.platform.operations.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkOrderSummaryDto(
        UUID id,
        UUID tenantId,
        Integer internalNumber,
        UUID vehicleId,
        String status,
        BigDecimal totalAmount,
        String currency
) {}
