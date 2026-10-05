package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record RegisterCompanyCustomerCommand(
        TenantId tenantId,
        String companyName,
        String taxId,
        String email,
        String phone
) {
    public RegisterCompanyCustomerCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(companyName, "Company name cannot be null");
        Objects.requireNonNull(taxId, "Tax ID cannot be null");
    }
}
