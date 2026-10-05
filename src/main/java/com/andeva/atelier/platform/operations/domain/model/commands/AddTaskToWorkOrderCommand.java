package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.ServiceId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.math.BigDecimal;
import java.util.UUID;

public record AddTaskToWorkOrderCommand(
        WorkOrderId workOrderId,
        ServiceId serviceId,
        UUID mechanicId,
        String description,
        BigDecimal price,
        BigDecimal estimatedHours,
        String currency
) {
}
