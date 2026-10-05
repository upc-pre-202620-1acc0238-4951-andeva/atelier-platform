package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.SubmitTaskProposalCommand;
import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.SubmitTaskProposalResource;

import java.util.UUID;

public final class SubmitTaskProposalCommandFromResourceAssembler {
    private SubmitTaskProposalCommandFromResourceAssembler() {}

    public static SubmitTaskProposalCommand toCommandFromResource(UUID workOrderId, SubmitTaskProposalResource resource) {
        return new SubmitTaskProposalCommand(
                new WorkOrderId(workOrderId),
                resource.mechanicId(),
                resource.description(),
                resource.severity() != null ? ProposalSeverity.valueOf(resource.severity()) : ProposalSeverity.MEDIUM,
                resource.imageUrl() != null ? StorageUrl.of(resource.imageUrl()) : null,
                resource.suggestedServiceId()
        );
    }
}
