package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.LocalDate;
import java.util.Objects;

public record GeneratePayrollCommand(
        TenantId tenantId,
        TenantMembershipId membershipId,
        LocalDate periodStart,
        LocalDate periodEnd
) {
    public GeneratePayrollCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        Objects.requireNonNull(periodStart, "periodStart cannot be null");
        Objects.requireNonNull(periodEnd, "periodEnd cannot be null");
    }
}
