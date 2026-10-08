package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record WorkOrderTaskHoldEvent(
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID mechanicId,
        String missingItemDescription,
        Instant occurredOn
) implements Serializable {
    public WorkOrderTaskHoldEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderTaskHoldEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, com.andeva.atelier.platform.operations.domain.model.enums.HoldReason reason, String missingItemDescription) {
        return new WorkOrderTaskHoldEvent(workOrderId, taskId, null, missingItemDescription, Instant.now());
    }

    public static WorkOrderTaskHoldEvent of(WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID mechanicId, String missingItemDescription) {
        return new WorkOrderTaskHoldEvent(workOrderId, taskId, mechanicId, missingItemDescription, Instant.now());
    }
}
