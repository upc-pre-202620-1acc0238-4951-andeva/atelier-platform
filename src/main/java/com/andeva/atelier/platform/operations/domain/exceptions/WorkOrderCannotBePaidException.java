package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

public class WorkOrderCannotBePaidException extends OperationsDomainException {

    public WorkOrderCannotBePaidException(WorkOrderId id, WorkOrderStatus currentStatus) {
        super("WORK_ORDER_CANNOT_BE_PAID", String.format("Work order %s cannot be marked as paid while in status %s. It must be COMPLETED", id != null ? id.value() : "null", currentStatus));
    }
}
