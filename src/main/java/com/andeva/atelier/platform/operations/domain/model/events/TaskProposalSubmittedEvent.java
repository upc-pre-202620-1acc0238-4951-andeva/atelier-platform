package com.andeva.atelier.platform.operations.domain.model.events;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record TaskProposalSubmittedEvent(
        WorkOrderId workOrderId,
        UUID proposalId,
        UUID mechanicId,
        ProposalSeverity severity,
        Instant occurredOn
) implements Serializable {
    public TaskProposalSubmittedEvent {
        Objects.requireNonNull(workOrderId, "workOrderId cannot be null");
        Objects.requireNonNull(proposalId, "proposalId cannot be null");
        Objects.requireNonNull(mechanicId, "mechanicId cannot be null");
        Objects.requireNonNull(severity, "severity cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static TaskProposalSubmittedEvent of(WorkOrderId workOrderId, UUID proposalId, UUID mechanicId, ProposalSeverity severity) {
        return new TaskProposalSubmittedEvent(workOrderId, proposalId, mechanicId, severity, Instant.now());
    }
}
