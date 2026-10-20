package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * The head mechanic asked the administration to suspend a task because of a defective part.
 */
public record TaskSuspensionRequestedEvent(
        TenantId tenantId,
        WorkOrderId workOrderId,
        WorkOrderTaskId taskId,
        UUID requestedBy,
        String reason,
        Instant occurredOn
) implements Serializable {
    public TaskSuspensionRequestedEvent {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(taskId, "taskId cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static TaskSuspensionRequestedEvent of(TenantId tenantId, WorkOrderId workOrderId, WorkOrderTaskId taskId, UUID requestedBy, String reason) {
        return new TaskSuspensionRequestedEvent(tenantId, workOrderId, taskId, requestedBy, reason, Instant.now());
    }
}
