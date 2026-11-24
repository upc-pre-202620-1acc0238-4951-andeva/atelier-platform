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
        BigDecimal consolidatedPrice,
        String currency,
        String holdReason,
        String missingItemDescription,
        Long totalPausedSeconds,
        // Start of the running pause; null unless the task is ON_HOLD
        Instant pausedAt,
        Instant startedAt,
        Instant completedAt,
        String pendingSuspensionReason,
        Instant pendingSuspensionRequestedAt,
        List<TaskProductResource> products,
        List<WorkOrderTaskImageResource> evidenceImages
) {
    public WorkOrderTaskResource(
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
            Instant pausedAt,
            Instant startedAt,
            Instant completedAt,
            String pendingSuspensionReason,
            Instant pendingSuspensionRequestedAt,
            List<TaskProductResource> products,
            List<WorkOrderTaskImageResource> evidenceImages
    ) {
        this(id, workOrderId, serviceId, serviceName, mechanicId, mechanicName, status, description,
                price, price, currency, holdReason, missingItemDescription, totalPausedSeconds,
                pausedAt, startedAt, completedAt, pendingSuspensionReason, pendingSuspensionRequestedAt,
                products, evidenceImages);
    }
}
