package com.andeva.atelier.platform.crm.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;

import java.util.Objects;

public record RevokeCustomerMemberCommand(TenantId tenantId, CustomerId customerId, UserId userId) {
    public RevokeCustomerMemberCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(customerId, "CustomerId cannot be null");
        Objects.requireNonNull(userId, "UserId cannot be null");
    }
}
