package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record UpdateCustomerContactCommand(
        TenantId tenantId,
        CustomerId customerId,
        String email,
        String phone
) {
    public UpdateCustomerContactCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
    }
}
