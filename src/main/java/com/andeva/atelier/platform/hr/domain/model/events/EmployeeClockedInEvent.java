package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.enums.AttendanceStatus;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record EmployeeClockedInEvent(
        AttendanceRecordId attendanceRecordId,
        TenantId tenantId,
        BranchId branchId,
        TenantMembershipId membershipId,
        WorkShiftId shiftId,
        AttendanceStatus status,
        Instant clockIn,
        Instant occurredOn
) implements Serializable {

    public EmployeeClockedInEvent {
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(branchId, "branchId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
        Objects.requireNonNull(clockIn, "clockIn cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static EmployeeClockedInEvent now(
            AttendanceRecordId attendanceRecordId,
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId,
            WorkShiftId shiftId,
            AttendanceStatus status,
            Instant clockIn
    ) {
        return new EmployeeClockedInEvent(attendanceRecordId, tenantId, branchId, membershipId, shiftId, status, clockIn, Instant.now());
    }
}
