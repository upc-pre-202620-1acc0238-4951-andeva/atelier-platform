package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

public class TaskNotOnHoldException extends OperationsDomainException {

    public TaskNotOnHoldException(WorkOrderTaskId taskId, WorkOrderTaskStatus currentStatus) {
        super("TASK_NOT_ON_HOLD", String.format("Task %s is not currently ON_HOLD (current status: %s) and cannot be resumed", taskId != null ? taskId.value() : "null", currentStatus));
    }
}
