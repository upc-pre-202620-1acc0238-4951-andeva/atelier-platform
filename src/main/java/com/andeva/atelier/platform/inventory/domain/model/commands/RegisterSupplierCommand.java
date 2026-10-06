package com.andeva.atelier.platform.inventory.domain.model.commands;

import com.andeva.atelier.platform.inventory.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.io.Serializable;
import java.util.Objects;

public record RegisterSupplierCommand(
        TenantId tenantId,
        String businessName,
        TaxId taxId,
        String contactName,
        String phone,
        String email,
        String address
) implements Serializable {

    public RegisterSupplierCommand {
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(businessName, "businessName cannot be null");
        Objects.requireNonNull(taxId, "taxId cannot be null");
    }
}
