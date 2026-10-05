package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;

public record RemoveProductFromTaskCommand(
        WorkOrderTaskId taskId,
        WorkOrderTaskProductId productItemId
) {
}
