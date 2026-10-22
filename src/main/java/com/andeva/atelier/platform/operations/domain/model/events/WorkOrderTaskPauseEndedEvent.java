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
 * The mechanic restarted the labor clock after a break or an end-of-shift pause.
 */
public record WorkOrderTaskPauseEndedEvent(
        TenantId tenantId,
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID mechanicId,
        HoldReason pauseReason,
        Instant occurredOn
) implements Serializable {
    public WorkOrderTaskPauseEndedEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkOrderTaskPauseEndedEvent of(TenantId tenantId, WorkOrderId workOrderId, WorkOrderTaskId taskId,
                                                 UUID mechanicId, HoldReason pauseReason) {
        return new WorkOrderTaskPauseEndedEvent(tenantId, workOrderId, taskId, mechanicId, pauseReason, Instant.now());
    }
}
