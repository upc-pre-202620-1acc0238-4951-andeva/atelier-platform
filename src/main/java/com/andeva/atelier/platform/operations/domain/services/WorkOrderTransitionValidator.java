package com.andeva.atelier.platform.operations.domain.services;

import com.andeva.atelier.platform.operations.domain.exceptions.InvalidWorkOrderStatusTransitionException;
import com.andeva.atelier.platform.operations.domain.exceptions.WorkOrderCannotBePaidException;
import com.andeva.atelier.platform.operations.domain.model.aggregates.WorkOrder;
import com.andeva.atelier.platform.operations.domain.model.entities.WorkOrderTask;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderStatus;
import com.andeva.atelier.platform.operations.domain.model.enums.WorkOrderTaskStatus;
import org.springframework.stereotype.Service;

@Service
public class WorkOrderTransitionValidator {

    public void validateTransition(WorkOrder workOrder, WorkOrderStatus targetStatus) {
        if (workOrder == null) {
            throw new IllegalArgumentException("WorkOrder cannot be null for transition validation");
        }

        WorkOrderStatus currentStatus = workOrder.getStatus();

        if (currentStatus == targetStatus) {
            return;
        }

        switch (targetStatus) {
            case IN_PROGRESS -> {
                if (currentStatus != WorkOrderStatus.DRAFT) {
                    throw new InvalidWorkOrderStatusTransitionException(workOrder.getId(), currentStatus, targetStatus);
                }
            }
            case COMPLETED -> {
                if (currentStatus != WorkOrderStatus.IN_PROGRESS) {
                    throw new InvalidWorkOrderStatusTransitionException(workOrder.getId(), currentStatus, targetStatus);
                }
                for (WorkOrderTask task : workOrder.getTasks()) {
                    if (task.getStatus() == WorkOrderTaskStatus.PENDING ||
                            task.getStatus() == WorkOrderTaskStatus.ASSIGNED ||
                            task.getStatus() == WorkOrderTaskStatus.IN_PROGRESS ||
                            task.getStatus() == WorkOrderTaskStatus.ON_HOLD) {
                        throw new InvalidWorkOrderStatusTransitionException(
                                String.format("Work order %s cannot transition to COMPLETED because task %s is still in status %s",
                                        workOrder.getId().value(), task.getId().value(), task.getStatus()));
                    }
                }
            }
            case PAID -> {
                if (currentStatus != WorkOrderStatus.COMPLETED) {
                    throw new WorkOrderCannotBePaidException(workOrder.getId(), currentStatus);
                }
            }
            case CANCELED -> {
                if (currentStatus == WorkOrderStatus.COMPLETED || currentStatus == WorkOrderStatus.PAID) {
                    throw new InvalidWorkOrderStatusTransitionException(workOrder.getId(), currentStatus, targetStatus);
                }
            }
            default -> throw new InvalidWorkOrderStatusTransitionException(workOrder.getId(), currentStatus, targetStatus);
        }
    }
}
