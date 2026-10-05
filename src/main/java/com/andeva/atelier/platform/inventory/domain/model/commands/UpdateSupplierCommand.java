package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;

import java.io.Serializable;
import java.util.Objects;

public record UpdateSupplierCommand(
        SupplierId supplierId,
        String businessName,
        String tradeName,
        String contactName,
        String phone,
        String email,
        String address,
        String status
) implements Serializable {

    public UpdateSupplierCommand {
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
    }

    public UpdateSupplierCommand(SupplierId supplierId, String businessName, String contactName, String phone, String email, String address) {
        this(supplierId, businessName, null, contactName, phone, email, address, null);
    }
}
