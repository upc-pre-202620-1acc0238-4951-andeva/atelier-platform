package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;

import java.io.Serializable;
import java.util.Objects;

public record DeactivateSupplierCommand(SupplierId supplierId) implements Serializable {
    public DeactivateSupplierCommand {
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
    }
}
