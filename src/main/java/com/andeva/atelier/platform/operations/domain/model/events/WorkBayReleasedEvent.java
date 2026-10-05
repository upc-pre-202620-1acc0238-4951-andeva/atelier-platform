package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public record WorkBayReleasedEvent(
        WorkOrderId workOrderId,
        WorkBayId bayId,
        Instant occurredOn
) implements Serializable {
    public WorkBayReleasedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(bayId, "bayId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static WorkBayReleasedEvent of(WorkOrderId workOrderId, WorkBayId bayId) {
        return new WorkBayReleasedEvent(workOrderId, bayId, Instant.now());
    }
}
