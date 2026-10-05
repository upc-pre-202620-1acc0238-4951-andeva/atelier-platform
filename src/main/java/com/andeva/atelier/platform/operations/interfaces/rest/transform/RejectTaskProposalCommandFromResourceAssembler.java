package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.RejectTaskProposalCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.RejectTaskProposalResource;

import java.util.UUID;

public final class RejectTaskProposalCommandFromResourceAssembler {
    private RejectTaskProposalCommandFromResourceAssembler() {}

    public static RejectTaskProposalCommand toCommandFromResource(UUID workOrderId, UUID proposalId, RejectTaskProposalResource resource) {
        return new RejectTaskProposalCommand(
                new WorkOrderId(workOrderId),
                proposalId,
                resource.customerNotes()
        );
    }
}
