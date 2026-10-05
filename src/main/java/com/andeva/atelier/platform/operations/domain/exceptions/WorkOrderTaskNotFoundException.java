package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderTaskId;

import java.util.UUID;

public class WorkOrderTaskNotFoundException extends OperationsDomainException {

    public WorkOrderTaskNotFoundException(WorkOrderTaskId taskId) {
        super("WORK_ORDER_TASK_NOT_FOUND", String.format("Work order task with identifier %s was not found", taskId != null ? taskId.value() : "null"));
    }

    public WorkOrderTaskNotFoundException(UUID taskId) {
        super("WORK_ORDER_TASK_NOT_FOUND", String.format("Work order task with identifier %s was not found", taskId));
    }
}
