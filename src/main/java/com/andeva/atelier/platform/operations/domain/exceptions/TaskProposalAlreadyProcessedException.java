package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalStatus;

import java.util.UUID;

public class TaskProposalAlreadyProcessedException extends OperationsDomainException {

    public TaskProposalAlreadyProcessedException(UUID proposalId, ProposalStatus currentStatus) {
        super("TASK_PROPOSAL_ALREADY_PROCESSED", String.format("Task proposal %s has already been processed with status %s", proposalId, currentStatus));
    }
}
