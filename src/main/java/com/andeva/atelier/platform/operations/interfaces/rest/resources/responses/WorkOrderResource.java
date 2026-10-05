package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkOrderResource(
        UUID id,
        UUID tenantId,
        Integer internalNumber,
        UUID vehicleId,
        UUID customerId,
        UUID branchId,
        UUID currentBayId,
        Integer mileageIn,
        String status,
        BigDecimal totalAmount,
        String currency
) {}
