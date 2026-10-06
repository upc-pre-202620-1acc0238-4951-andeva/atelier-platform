package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.hr.domain.model.ids.EmployeeProfileId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetEmployeeProfileByIdQuery(TenantId tenantId, EmployeeProfileId profileId) {
    public GetEmployeeProfileByIdQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(profileId, "EmployeeProfileId cannot be null");
    }
}
