package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record RegisterIndividualCustomerCommand(
        TenantId tenantId,
        String firstName,
        String lastName,
        String taxId,
        String email,
        String phone
) {
    public RegisterIndividualCustomerCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(firstName, "First name cannot be null");
        Objects.requireNonNull(lastName, "Last name cannot be null");
        Objects.requireNonNull(taxId, "Tax ID cannot be null");
    }
}
