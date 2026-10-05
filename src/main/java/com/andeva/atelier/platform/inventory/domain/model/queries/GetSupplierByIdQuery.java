package com.andeva.atelier.platform.inventory.domain.model.queries;

import com.andeva.atelier.platform.inventory.domain.model.ids.SupplierId;

import java.util.Objects;

public record GetSupplierByIdQuery(
        SupplierId supplierId
) {
    public GetSupplierByIdQuery {
        Objects.requireNonNull(supplierId, "supplierId cannot be null");
    }
}
