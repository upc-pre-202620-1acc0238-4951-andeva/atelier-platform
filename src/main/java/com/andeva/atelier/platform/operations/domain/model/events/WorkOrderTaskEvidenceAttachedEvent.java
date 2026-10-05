package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderTaskEvidenceAttachedEvent(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID imageId,
        StorageUrl imageUrl,
        EvidenceType evidenceType,
        Instant occurredOn
) implements Serializable {
    public WorkOrderTaskEvidenceAttachedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(imageId, "imageId cannot be null");
        Objects.requireNonNull(imageUrl, "imageUrl cannot be null");
        Objects.requireNonNull(evidenceType, "evidenceType cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderTaskEvidenceAttachedEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID imageId, StorageUrl imageUrl, EvidenceType evidenceType) {
        return new WorkOrderTaskEvidenceAttachedEvent(workOrderId, taskId, imageId, imageUrl, evidenceType, Instant.now());
    }
}
