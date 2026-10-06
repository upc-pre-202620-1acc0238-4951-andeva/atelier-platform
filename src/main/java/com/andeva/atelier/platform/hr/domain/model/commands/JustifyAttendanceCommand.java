package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record JustifyAttendanceCommand(
        TenantId tenantId,
        AttendanceRecordId attendanceRecordId,
        TenantMembershipId supervisorMembershipId,
        String reason
) {
    public JustifyAttendanceCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(attendanceRecordId, "AttendanceRecordId cannot be null");
        Objects.requireNonNull(supervisorMembershipId, "supervisorMembershipId cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
    }
}
