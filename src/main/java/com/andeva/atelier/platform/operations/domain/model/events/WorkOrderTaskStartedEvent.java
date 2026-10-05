package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderTaskStartedEvent(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID mechanicId,
        Instant occurredOn
) implements Serializable {
    public WorkOrderTaskStartedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderTaskStartedEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID mechanicId) {
        return new WorkOrderTaskStartedEvent(workOrderId, taskId, mechanicId, Instant.now());
    }
}
