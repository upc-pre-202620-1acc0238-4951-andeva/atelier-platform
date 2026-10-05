package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.LaborHours;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderTaskCompletedEvent(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID mechanicId,
        LaborHours actualHours,
        Instant occurredOn
) implements Serializable {
    public WorkOrderTaskCompletedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(actualHours, "actualHours cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderTaskCompletedEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID mechanicId, LaborHours actualHours) {
        return new WorkOrderTaskCompletedEvent(workOrderId, taskId, mechanicId, actualHours, Instant.now());
    }
}
