package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The mechanic stopped the labor clock for a break or the end of the shift.
 */
public record WorkOrderTaskPausedEvent(
        TenantId tenantId,
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID mechanicId,
        HoldReason reason,
        Instant occurredOn
) implements Serializable {
    public WorkOrderTaskPausedEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderTaskPausedEvent of(TenantId tenantId, WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID mechanicId, HoldReason reason) {
        return new WorkOrderTaskPausedEvent(tenantId, workOrderId, taskId, mechanicId, reason, Instant.now());
    }
}
