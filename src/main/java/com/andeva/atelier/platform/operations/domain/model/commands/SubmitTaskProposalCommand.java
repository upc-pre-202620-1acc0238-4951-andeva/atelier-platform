package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.enums.ProposalSeverity;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;
import com.andeva.atelier.platform.operations.domain.model.valueobjects.StorageUrl;

import java.util.UUID;

public record SubmitTaskProposalCommand(
        WorkOrderId workOrderId,
        UUID mechanicId,
        String description,
        ProposalSeverity severity,
        StorageUrl imageUrl,
        UUID suggestedServiceId
) {
}
