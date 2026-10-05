package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderIntakeImageAttachedEvent(
        WorkOrderId workOrderId,
        UUID imageId,
        StorageUrl imageUrl,
        Instant occurredOn
) implements Serializable {
    public WorkOrderIntakeImageAttachedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(imageId, "imageId cannot be null");
        Objects.requireNonNull(imageUrl, "imageUrl cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderIntakeImageAttachedEvent of(WorkOrderId workOrderId, UUID imageId, StorageUrl imageUrl) {
        return new WorkOrderIntakeImageAttachedEvent(workOrderId, imageId, imageUrl, Instant.now());
    }
}
