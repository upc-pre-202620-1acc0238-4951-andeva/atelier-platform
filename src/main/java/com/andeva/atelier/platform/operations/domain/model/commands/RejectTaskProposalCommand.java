package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.util.UUID;

public record RejectTaskProposalCommand(
        WorkOrderId workOrderId,
        UUID proposalId,
        String customerNotes
) {
}
