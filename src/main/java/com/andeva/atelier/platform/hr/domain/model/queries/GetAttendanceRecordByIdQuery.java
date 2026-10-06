package com.andeva.atelier.platform.hr.domain.model.queries;

import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.Objects;

public record GetAttendanceRecordByIdQuery(TenantId tenantId, AttendanceRecordId attendanceRecordId) {
    public GetAttendanceRecordByIdQuery {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(attendanceRecordId, "attendanceRecordId cannot be null");
    }
}
