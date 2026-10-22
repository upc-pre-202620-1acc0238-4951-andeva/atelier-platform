package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

/**
 * @param clockInTime moment captured on the device for a clock-in queued without coverage (null = now)
 */
public record RecordClockInCommand(
        TenantId tenantId,
        BranchId branchId,
        TenantMembershipId membershipId,
        WorkShiftId shiftId,
        GeoCoordinates coordinates,
        Instant clockInTime
) {
    public RecordClockInCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(branchId, "BranchId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        Objects.requireNonNull(shiftId, "WorkShiftId cannot be null");
        Objects.requireNonNull(coordinates, "GeoCoordinates cannot be null");
    }

    public RecordClockInCommand(TenantId tenantId, BranchId branchId, TenantMembershipId membershipId,
                                WorkShiftId shiftId, GeoCoordinates coordinates) {
        this(tenantId, branchId, membershipId, shiftId, coordinates, null);
    }
}
