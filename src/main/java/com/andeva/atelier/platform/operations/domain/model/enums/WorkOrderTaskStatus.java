package com.andeva.atelier.platform.operations.domain.model.enums;

public enum WorkOrderTaskStatus {
    PENDING,
    ASSIGNED,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    CANCELLED;

    public boolean isActive() {
        return this == ASSIGNED || this == IN_PROGRESS || this == ON_HOLD;
    }
}
