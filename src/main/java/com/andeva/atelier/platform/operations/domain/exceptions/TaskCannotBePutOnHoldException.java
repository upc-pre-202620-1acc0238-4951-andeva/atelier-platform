package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

public class TaskCannotBePutOnHoldException extends OperationsDomainException {

    public TaskCannotBePutOnHoldException(WorkOrderTaskId taskId, WorkOrderTaskStatus currentStatus) {
        super("TASK_CANNOT_BE_PUT_ON_HOLD", String.format("Task %s cannot be put on hold while in status %s. Must be IN_PROGRESS", taskId != null ? taskId.value() : "null", currentStatus));
    }
}
