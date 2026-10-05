package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WorkOrderSummaryResource(
        UUID id,
        UUID tenantId,
        Integer internalNumber,
        UUID vehicleId,
        UUID customerId,
        UUID branchId,
        UUID currentBayId,
        String bayName,
        String status,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {}
