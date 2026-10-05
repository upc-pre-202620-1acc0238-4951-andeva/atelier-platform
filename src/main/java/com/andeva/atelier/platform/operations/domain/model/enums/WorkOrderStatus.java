package com.andeva.atelier.platform.operations.domain.model.enums;

public enum WorkOrderStatus {
    DRAFT,
    IN_PROGRESS,
    COMPLETED,
    PAID,
    CANCELED;

    public boolean isTerminal() {
        return this == PAID || this == CANCELED;
    }

    public boolean isMutable() {
        return this == DRAFT || this == IN_PROGRESS;
    }
}
