package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.entities.TaskProposal;
import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.responses.TaskProposalResource;

public final class TaskProposalResourceAssembler {
    private TaskProposalResourceAssembler() {}

    public static TaskProposalResource toResourceFromEntity(TaskProposal proposal, String serviceName, String mechanicName) {
        if (proposal == null) {
            return null;
        }
        return new TaskProposalResource(
                proposal.getId(),
                proposal.getWorkOrderId() != null ? proposal.getWorkOrderId().value() : null,
                proposal.getTaskId().map(WorkOrderTaskId::value).orElse(null),
                proposal.getServiceId().map(ServiceId::value).orElse(null),
                serviceName,
                proposal.getMechanicId(),
                mechanicName,
                proposal.getDescription(),
                proposal.getSeverity() != null ? proposal.getSeverity().name() : null,
                proposal.getImageUrl() != null ? proposal.getImageUrl().value() : null,
                proposal.getStatus() != null ? proposal.getStatus().name() : null,
                proposal.getCustomerNotes().orElse(null),
                proposal.getCreatedAt(),
                proposal.getUpdatedAt()
        );
    }
}
