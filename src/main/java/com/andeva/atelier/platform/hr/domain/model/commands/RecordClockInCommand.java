package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record RecordClockInCommand(
        TenantId tenantId,
        BranchId branchId,
        TenantMembershipId membershipId,
        WorkShiftId shiftId,
        GeoCoordinates coordinates
) {
    public RecordClockInCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        Objects.requireNonNull(shiftId, "WorkShiftId cannot be null");
        Objects.requireNonNull(coordinates, "GeoCoordinates cannot be null");
    }
}
