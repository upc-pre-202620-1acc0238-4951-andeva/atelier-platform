package com.andeva.atelier.platform.inventory.domain.model.enums;

public enum InventoryItemStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED;

    public boolean isActive() {
        return this == ACTIVE;
    }
}
