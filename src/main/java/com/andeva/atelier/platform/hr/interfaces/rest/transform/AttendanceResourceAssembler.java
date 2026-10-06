package com.andeva.atelier.platform.hr.interfaces.rest.transform;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.commands.JustifyAttendanceCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockInCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockOutCommand;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.ClockInRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.ClockOutRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.requests.JustifyAttendanceRequest;
import com.andeva.atelier.platform.hr.interfaces.rest.resources.responses.AttendanceResource;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AttendanceResourceAssembler {

    private AttendanceResourceAssembler() {}

    public static RecordClockInCommand toCommand(TenantId tenantId, TenantMembershipId membershipId, ClockInRequest request) {
        return new RecordClockInCommand(
                tenantId,
                BranchId.of(request.branchId()),
                membershipId,
                WorkShiftId.of(request.shiftId()),
                GeoCoordinates.of(request.latitude(), request.longitude())
        );
    }

    public static RecordClockOutCommand toCommand(TenantId tenantId, TenantMembershipId membershipId, UUID attendanceId, ClockOutRequest request) {
        UUID effectiveId = request != null && request.attendanceId() != null ? request.attendanceId() : attendanceId;
        Instant effectiveTime = request != null && request.clockOutTime() != null ? request.clockOutTime() : Instant.now();
        return new RecordClockOutCommand(
                tenantId,
                effectiveId != null ? AttendanceRecordId.of(effectiveId) : null,
                membershipId,
                effectiveTime
        );
    }

    public static JustifyAttendanceCommand toCommand(TenantId tenantId, AttendanceRecordId attendanceId, TenantMembershipId supervisorMembershipId, JustifyAttendanceRequest request) {
        return new JustifyAttendanceCommand(
                tenantId,
                attendanceId,
                supervisorMembershipId,
                request.reason()
        );
    }

    public static AttendanceResource toResource(AttendanceRecord domain) {
        if (domain == null) return null;
        UUID justifiedBy = domain.getJustification().map(j -> j.justifiedBy().value()).orElse(null);
        Instant justifiedAt = domain.getJustification().map(j -> j.justifiedAt()).orElse(null);
        String reason = domain.getJustification().map(j -> j.reason()).orElse(null);

        return new AttendanceResource(
                domain.getId().value(),
                domain.getBranchId().value(),
                domain.getMembershipId().value(),
                domain.getShiftId().value(),
                domain.getClockIn(),
                domain.getClockOut(),
                domain.getStatus().name(),
                domain.getCoordinates().latitude(),
                domain.getCoordinates().longitude(),
                domain.getDistanceToBranch().meters(),
                reason,
                justifiedBy,
                justifiedAt
        );
    }

    public static List<AttendanceResource> toResourceList(List<AttendanceRecord> list) {
        if (list == null) return List.of();
        return list.stream().map(AttendanceResourceAssembler::toResource).toList();
    }
}
