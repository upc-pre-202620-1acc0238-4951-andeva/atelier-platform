package com.andeva.atelier.platform.operations.interfaces.rest.transform;

import com.andeva.atelier.platform.operations.domain.model.commands.AttachTaskEvidenceImageCommand;
import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.interfaces.rest.resources.requests.AttachTaskEvidenceResource;

import java.util.UUID;

public final class AttachTaskEvidenceCommandFromResourceAssembler {
    private AttachTaskEvidenceCommandFromResourceAssembler() {}

    public static AttachTaskEvidenceImageCommand toCommandFromResource(UUID taskId, AttachTaskEvidenceResource resource) {
        return new AttachTaskEvidenceImageCommand(
                new WorkOrderTaskId(taskId),
                resource.imageUrl(),
                EvidenceType.IN_PROGRESS,
                resource.description()
        );
    }
}
