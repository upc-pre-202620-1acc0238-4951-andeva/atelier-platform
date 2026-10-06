package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.enums.EmploymentStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record UpdateEmploymentStatusCommand(
        TenantId tenantId,
        EmployeeProfileId profileId,
        EmploymentStatus newStatus
) {
    public UpdateEmploymentStatusCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(profileId, "EmployeeProfileId cannot be null");
        Objects.requireNonNull(newStatus, "EmploymentStatus cannot be null");
    }
}
