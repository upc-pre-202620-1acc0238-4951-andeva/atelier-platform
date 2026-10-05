package com.andeva.atelier.platform.operations.domain.exceptions;

import com.andeva.atelier.platform.operations.domain.model.ids.WorkBayId;

import java.util.UUID;

public class WorkBayNotFoundException extends OperationsDomainException {

    public WorkBayNotFoundException(WorkBayId bayId) {
        super("WORK_BAY_NOT_FOUND", String.format("Work bay with identifier %s was not found", bayId != null ? bayId.value() : "null"));
    }

    public WorkBayNotFoundException(UUID bayId) {
        super("WORK_BAY_NOT_FOUND", String.format("Work bay with identifier %s was not found", bayId));
    }
}
