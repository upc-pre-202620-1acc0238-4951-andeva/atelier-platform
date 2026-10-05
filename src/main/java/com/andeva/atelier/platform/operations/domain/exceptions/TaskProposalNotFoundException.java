package com.andeva.atelier.platform.operations.domain.exceptions;

import java.util.UUID;

public class TaskProposalNotFoundException extends OperationsDomainException {

    public TaskProposalNotFoundException(UUID proposalId) {
        super("TASK_PROPOSAL_NOT_FOUND", String.format("Task proposal with identifier %s was not found in work order", proposalId));
    }
}
