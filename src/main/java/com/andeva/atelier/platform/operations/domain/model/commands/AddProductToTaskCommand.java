package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.math.BigDecimal;
import java.util.UUID;

public record AddProductToTaskCommand(
        WorkOrderTaskId taskId,
        UUID productId,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String currency
) {
}
