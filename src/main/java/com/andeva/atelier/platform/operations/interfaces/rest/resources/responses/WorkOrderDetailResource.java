package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkOrderDetailResource(
        UUID id,
        UUID tenantId,
        Integer internalNumber,
        UUID vehicleId,
        UUID customerId,
        UUID branchId,
        UUID currentBayId,
        String bayName,
        Integer mileageIn,
        String diagnosticSummary,
        String status,
        BigDecimal totalAmount,
        String currency,
        List<WorkOrderTaskResource> tasks,
        List<TaskProposalResource> proposals,
        List<WorkOrderImageResource> intakeImages,
        Instant createdAt,
        Instant updatedAt
) {}
