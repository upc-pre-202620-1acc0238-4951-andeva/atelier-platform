package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.enums.CompensationType;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record UpdateSalaryCommand(
        TenantId tenantId,
        EmployeeProfileId profileId,
        Money newSalary,
        CompensationType compensationType
) {
    public UpdateSalaryCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(profileId, "EmployeeProfileId cannot be null");
        Objects.requireNonNull(newSalary, "newSalary cannot be null");
    }
}
