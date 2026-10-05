package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderTaskImageResource(
        UUID id,
        UUID taskId,
        String imageUrl,
        String description,
        Instant uploadedAt
) {}
