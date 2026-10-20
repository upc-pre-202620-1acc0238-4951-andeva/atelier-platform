package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

import java.util.Objects;

/**
 * The employee submits the reason of a late or missed attendance for review.
 */
public record SubmitAttendanceJustificationCommand(
        TenantId tenantId,
        AttendanceRecordId attendanceRecordId,
        TenantMembershipId employeeMembershipId,
        String reason
) {
    public SubmitAttendanceJustificationCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(attendanceRecordId, "AttendanceRecordId cannot be null");
        Objects.requireNonNull(employeeMembershipId, "employeeMembershipId cannot be null");
        Objects.requireNonNull(reason, "reason cannot be null");
    }
}
