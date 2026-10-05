package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.math.BigDecimal;

public record CompleteWorkOrderTaskCommand(
        WorkOrderTaskId taskId,
        BigDecimal actualHours,
        String notes
) {
}
