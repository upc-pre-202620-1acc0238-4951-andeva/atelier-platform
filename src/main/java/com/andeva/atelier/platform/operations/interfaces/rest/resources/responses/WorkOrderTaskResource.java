package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WorkOrderTaskResource(
        UUID id,
        UUID workOrderId,
        UUID serviceId,
        String serviceName,
        UUID mechanicId,
        String mechanicName,
        String status,
        String description,
        BigDecimal price,
        String currency,
        String holdReason,
        String missingItemDescription,
        Long totalPausedSeconds,
        Instant startedAt,
        Instant completedAt,
        List<TaskProductResource> products,
        List<WorkOrderTaskImageResource> evidenceImages
) {}
