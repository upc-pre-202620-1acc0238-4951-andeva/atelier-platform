package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TaskProposalRejectedEvent(
        WorkOrderId workOrderId,
        UUID proposalId,
        String customerNotes,
        Instant occurredOn
) implements Serializable {
    public TaskProposalRejectedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(proposalId, "proposalId cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static TaskProposalRejectedEvent of(WorkOrderId workOrderId, UUID proposalId, String customerNotes) {
        return new TaskProposalRejectedEvent(workOrderId, proposalId, customerNotes, Instant.now());
    }
}
