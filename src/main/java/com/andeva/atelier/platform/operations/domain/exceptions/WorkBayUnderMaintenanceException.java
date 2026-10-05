package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;

public class WorkBayUnderMaintenanceException extends OperationsDomainException {

    public WorkBayUnderMaintenanceException(WorkBayId bayId) {
        super("WORK_BAY_UNDER_MAINTENANCE", String.format("Work bay with identifier %s is under maintenance or out of service", bayId != null ? bayId.value() : "null"));
    }
}
