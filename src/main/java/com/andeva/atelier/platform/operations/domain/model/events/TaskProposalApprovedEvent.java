package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TaskProposalApprovedEvent(
        WorkOrderId workOrderId,
        UUID proposalId,
        WorkOrderTaskId createdTaskId,
        Instant occurredOn
) implements Serializable {
    public TaskProposalApprovedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(proposalId, "proposalId cannot be null");
        Objects.requireNonNull(createdTaskId, "createdTaskId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static TaskProposalApprovedEvent of(WorkOrderId workOrderId, UUID proposalId, WorkOrderTaskId createdTaskId) {
        return new TaskProposalApprovedEvent(workOrderId, proposalId, createdTaskId, Instant.now());
    }
}
