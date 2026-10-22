package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskProductId;

import java.util.UUID;

/**
 * @param removedBy user withdrawing the part (kept in the inventory audit trail; may be null)
 */
public record RemoveProductFromTaskCommand(
        WorkOrderTaskId taskId,
        WorkOrderTaskProductId productItemId,
        UUID removedBy
) {
    public RemoveProductFromTaskCommand(WorkOrderTaskId taskId, WorkOrderTaskProductId productItemId) {
        this(taskId, productItemId, null);
    }
}
