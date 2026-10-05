package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

public record AssignWorkBayCommand(
        WorkOrderId workOrderId,
        WorkBayId bayId
) {
}
