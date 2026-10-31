package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Task assigned to the authenticated mechanic, with the work order context needed in the bay.
 */
public record AssignedTaskResource(
        UUID taskId,
        UUID workOrderId,
        Integer workOrderNumber,
        String workOrderStatus,
        UUID vehicleId,
        UUID branchId,
        UUID bayId,
        String status,
        String description,
        BigDecimal estimatedHours,
        Instant startedAt,
        long totalPausedSeconds,
        // Start of the running pause; null unless the task is ON_HOLD
        Instant pausedAt
) {}
