package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.enums.HoldReason;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

/**
 * Pause of the labor clock taken by the mechanic (BREAK or END_OF_SHIFT).
 */
public record PauseWorkOrderTaskCommand(
        WorkOrderTaskId taskId,
        HoldReason reason
) {
}
