package com.andeva.atelier.platform.operations.application.queryservices;

import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;

import java.util.Objects;

/**
 * Read model pairing a mechanic's task with the work order that holds it.
 */
public record AssignedTaskView(WorkOrder workOrder, WorkOrderTask task) {
    public AssignedTaskView {
        Objects.requireNonNull(workOrder, "workOrder cannot be null");
        Objects.requireNonNull(task, "task cannot be null");
    }
}
