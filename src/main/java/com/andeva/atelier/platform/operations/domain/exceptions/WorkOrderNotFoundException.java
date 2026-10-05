package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

import java.util.UUID;

public class WorkOrderNotFoundException extends OperationsDomainException {

    public WorkOrderNotFoundException(WorkOrderId workOrderId) {
        super("WORK_ORDER_NOT_FOUND", String.format("Work order with identifier %s was not found", workOrderId != null ? workOrderId.value() : "null"));
    }

    public WorkOrderNotFoundException(UUID workOrderId) {
        super("WORK_ORDER_NOT_FOUND", String.format("Work order with identifier %s was not found", workOrderId));
    }
}
