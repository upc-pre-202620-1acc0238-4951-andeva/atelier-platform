package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.math.BigDecimal;
import java.util.UUID;

public record ApproveTaskProposalCommand(
        WorkOrderId workOrderId,
        UUID proposalId,
        ServiceId serviceId,
        BigDecimal finalPrice,
        BigDecimal laborHours,
        UUID mechanicId,
        String notes
) {
}
