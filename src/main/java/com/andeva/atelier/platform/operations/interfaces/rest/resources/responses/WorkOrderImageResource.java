package com.andeva.atelier.platform.operations.interfaces.rest.resources.responses;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderImageResource(
        UUID id,
        UUID workOrderId,
        String imageUrl,
        String description,
        Instant uploadedAt
) {}
