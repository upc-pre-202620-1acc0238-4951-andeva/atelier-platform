package com.andeva.atelier.platform.operations.domain.model.commands;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

public record ResumeWorkOrderTaskCommand(
        WorkOrderTaskId taskId
) {
}
