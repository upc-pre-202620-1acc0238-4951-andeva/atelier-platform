package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.ApproveTaskProposalCommand;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.ApproveTaskProposalResource;

import java.util.UUID;

public final class ApproveTaskProposalCommandFromResourceAssembler {
    private ApproveTaskProposalCommandFromResourceAssembler() {}

    public static ApproveTaskProposalCommand toCommandFromResource(UUID workOrderId, UUID proposalId, ApproveTaskProposalResource resource) {
        return new ApproveTaskProposalCommand(
                new WorkOrderId(workOrderId),
                proposalId,
                new ServiceId(resource.serviceId()),
                resource.finalPrice(),
                resource.laborHours(),
                resource.mechanicId(),
                resource.notes()
        );
    }
}
