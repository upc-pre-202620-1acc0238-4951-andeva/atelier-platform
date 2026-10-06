package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record RegisterEmployeeProfileCommand(
        TenantId tenantId,
        BranchId branchId,
        TenantMembershipId membershipId,
        WorkShiftId shiftId,
        Money baseSalary,
        CompensationType compensationType,
        String jobTitle
) {
    public RegisterEmployeeProfileCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        Objects.requireNonNull(baseSalary, "baseSalary cannot be null");
        Objects.requireNonNull(jobTitle, "jobTitle cannot be null");
    }
}
