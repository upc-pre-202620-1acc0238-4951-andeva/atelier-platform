package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record DeactivateCustomerCommand(TenantId tenantId, CustomerId customerId) {
    public DeactivateCustomerCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
    }
}
