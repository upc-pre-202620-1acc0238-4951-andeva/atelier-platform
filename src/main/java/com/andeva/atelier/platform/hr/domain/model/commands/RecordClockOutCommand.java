package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record RecordClockOutCommand(
        TenantId tenantId,
        AttendanceRecordId attendanceRecordId,
        TenantMembershipId membershipId,
        Instant clockOutTime
) {
    public RecordClockOutCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
    }
}
