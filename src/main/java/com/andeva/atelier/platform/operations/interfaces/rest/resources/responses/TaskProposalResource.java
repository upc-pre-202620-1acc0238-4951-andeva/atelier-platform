package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

public record TaskProposalResource(
        UUID id,
        UUID workOrderId,
        UUID taskId,
        UUID serviceId,
        String serviceName,
        UUID mechanicId,
        String mechanicName,
        String description,
        String severity,
        String imageUrl,
        String status,
        String customerNotes,
        Instant createdAt,
        Instant updatedAt
) {}
