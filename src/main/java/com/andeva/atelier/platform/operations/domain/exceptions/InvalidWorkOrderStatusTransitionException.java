package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.ids.WorkOrderId;

public class InvalidWorkOrderStatusTransitionException extends OperationsDomainException {

    public InvalidWorkOrderStatusTransitionException(WorkOrderId id, WorkOrderStatus currentStatus, WorkOrderStatus targetStatus) {
        super("INVALID_WORK_ORDER_STATUS_TRANSITION",
                String.format("Work order %s cannot transition from %s to %s",
                        id != null ? id.value() : "null", currentStatus, targetStatus));
    }

    public InvalidWorkOrderStatusTransitionException(String message) {
        super("INVALID_WORK_ORDER_STATUS_TRANSITION", message);
    }
}
