package com.andeva.atelier.platform.inventory.domain.model.enums;

public enum PurchaseOrderStatus {
    DRAFT,
    ISSUED,
    RECEIVED,
    CANCELED;

    public boolean isTerminal() {
        return this == RECEIVED || this == CANCELED;
    }

    public boolean canBeModified() {
        return this == DRAFT;
    }

    public boolean canBeIssued() {
        return this == DRAFT;
    }

    public boolean canBeReceived() {
        return this == ISSUED;
    }

    public boolean canBeCanceled() {
        return this == DRAFT || this == ISSUED;
    }
}
