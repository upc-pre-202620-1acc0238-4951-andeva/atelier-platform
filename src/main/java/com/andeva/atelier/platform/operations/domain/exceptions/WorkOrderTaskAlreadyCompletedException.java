package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

public class WorkOrderTaskAlreadyCompletedException extends OperationsDomainException {

    public WorkOrderTaskAlreadyCompletedException(WorkOrderTaskId taskId) {
        super("WORK_ORDER_TASK_ALREADY_COMPLETED", String.format("Work order task with identifier %s has already been completed and cannot be modified", taskId != null ? taskId.value() : "null"));
    }
}
