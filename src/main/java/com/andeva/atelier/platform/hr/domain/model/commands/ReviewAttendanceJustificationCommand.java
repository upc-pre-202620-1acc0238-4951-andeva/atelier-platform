package com.andeva.atelier.platform.hr.domain.model.commands;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;

import java.util.Objects;

/**
 * The administration approves or dismisses a submitted justification.
 */
public record ReviewAttendanceJustificationCommand(
        TenantId tenantId,
        AttendanceRecordId attendanceRecordId,
        TenantMembershipId reviewerMembershipId,
        boolean approved
) {
    public ReviewAttendanceJustificationCommand {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(attendanceRecordId, "AttendanceRecordId cannot be null");
        Objects.requireNonNull(reviewerMembershipId, "reviewerMembershipId cannot be null");
    }
}
