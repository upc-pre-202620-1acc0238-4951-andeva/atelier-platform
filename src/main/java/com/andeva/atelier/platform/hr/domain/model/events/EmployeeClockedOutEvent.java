package com.andeva.atelier.platform.hr.domain.model.events;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import java.io.Serializable;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.Objects;

public record EmployeeClockedOutEvent(
        AttendanceRecordId attendanceRecordId,
        TenantId tenantId,
        TenantMembershipId membershipId,
        Instant clockOut,
        long totalWorkedMinutes,
        Instant occurredOn
) implements Serializable {

    public EmployeeClockedOutEvent {
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
        Objects.requireNonNull(membershipId, "membershipId cannot be null");
        Objects.requireNonNull(clockOut, "clockOut cannot be null");
        Objects.requireNonNull(occurredOn, "occurredOn cannot be null");
    }

    public static EmployeeClockedOutEvent now(
            AttendanceRecordId attendanceRecordId,
            TenantId tenantId,
            TenantMembershipId membershipId,
            Instant clockOut,
            long totalWorkedMinutes
    ) {
        return new EmployeeClockedOutEvent(attendanceRecordId, tenantId, membershipId, clockOut, totalWorkedMinutes, Instant.now());
    }
}
