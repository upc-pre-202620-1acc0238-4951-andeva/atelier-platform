package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.enums.EvidenceType;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

public record AttachTaskEvidenceImageCommand(
        WorkOrderTaskId taskId,
        String imageUrl,
        EvidenceType evidenceType,
        String description
) {
}
