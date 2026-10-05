package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;

public class WorkBayOccupiedException extends OperationsDomainException {

    public WorkBayOccupiedException(WorkBayId bayId) {
        super("WORK_BAY_OCCUPIED", String.format("Work bay with identifier %s is currently occupied by another active work order", bayId != null ? bayId.value() : "null"));
    }
}
