package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

public record CancelWorkOrderCommand(
        WorkOrderId workOrderId,
        String reason
) {
}
