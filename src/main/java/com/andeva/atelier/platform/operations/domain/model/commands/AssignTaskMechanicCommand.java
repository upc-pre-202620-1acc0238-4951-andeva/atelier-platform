package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.util.UUID;

public record AssignTaskMechanicCommand(
        WorkOrderTaskId taskId,
        UUID mechanicId
) {
}
