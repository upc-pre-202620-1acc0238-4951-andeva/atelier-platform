package com.andeva.atelier.platform.hr.domain.model.aggregates;

import com.andeva.atelier.platform.hr.domain.exceptions.AttendanceNotJustifiableException;
import com.andeva.atelier.platform.hr.domain.exceptions.GeofenceViolationException;
import com.andeva.atelier.platform.hr.domain.exceptions.InvalidAttendanceClockOutException;
import com.andeva.atelier.platform.hr.domain.model.enums.AttendanceStatus;
import com.andeva.atelier.platform.hr.domain.model.events.*;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.hr.domain.model.ids.WorkShiftId;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.AttendanceJustification;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.HaversineDistance;
import com.andeva.atelier.platform.hr.domain.services.HaversineGeofencingService;
import com.andeva.atelier.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

public class AttendanceRecord extends AbstractDomainAggregateRoot<AttendanceRecord> {

    private final AttendanceRecordId id;
    private final TenantId tenantId;
    private final BranchId branchId;
    private final TenantMembershipId membershipId;
    private final WorkShiftId shiftId;
    private final Instant clockIn;
    private Instant clockOut;
    private AttendanceStatus status;
    private final GeoCoordinates coordinates;
    private final HaversineDistance distanceToBranch;
    private AttendanceJustification justification;

    public AttendanceRecord(
            AttendanceRecordId id,
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId,
            WorkShiftId shiftId,
            Instant clockIn,
            Instant clockOut,
            AttendanceStatus status,
            GeoCoordinates coordinates,
            HaversineDistance distanceToBranch,
            AttendanceJustification justification
    ) {
        this.id = Objects.requireNonNull(id, "AttendanceRecordId cannot be null");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        this.branchId = Objects.requireNonNull(branchId, "BranchId cannot be null");
        this.membershipId = Objects.requireNonNull(membershipId, "TenantMembershipId cannot be null");
        this.shiftId = Objects.requireNonNull(shiftId, "WorkShiftId cannot be null");
        this.clockIn = Objects.requireNonNull(clockIn, "clockIn cannot be null");
        this.clockOut = clockOut;
        this.status = Objects.requireNonNull(status, "status cannot be null");
        this.coordinates = Objects.requireNonNull(coordinates, "coordinates cannot be null");
        this.distanceToBranch = Objects.requireNonNull(distanceToBranch, "distanceToBranch cannot be null");
        this.justification = justification;
    }

    public static AttendanceRecord recordClockIn(
            TenantId tenantId,
            BranchId branchId,
            TenantMembershipId membershipId,
            WorkShift workShift,
            GeoCoordinates employeeLocation,
            GeoCoordinates branchCentroid,
            double allowedRadiusMeters,
            HaversineGeofencingService geofencingService,
            ZoneId timeZone
    ) {
        Objects.requireNonNull(geofencingService, "HaversineGeofencingService cannot be null");
        HaversineDistance distance = geofencingService.calculateDistance(employeeLocation, branchCentroid);

        if (!distance.isWithin(allowedRadiusMeters)) {
            throw new GeofenceViolationException(distance.meters(), allowedRadiusMeters);
        }

        AttendanceRecordId attendanceId = AttendanceRecordId.generate();
        Instant now = Instant.now();
        LocalTime localClockIn = now.atZone(timeZone != null ? timeZone : ZoneId.systemDefault()).toLocalTime();

        AttendanceStatus status;
        long delayMinutes = 0;
        if (workShift.isLate(localClockIn)) {
            status = AttendanceStatus.LATE;
            LocalTime scheduledStart = workShift.getSchedule().startTime();
            delayMinutes = Duration.between(scheduledStart, localClockIn).toMinutes();
        } else {
            status = AttendanceStatus.ON_TIME;
        }

        AttendanceRecord record = new AttendanceRecord(
                attendanceId, tenantId, branchId, membershipId, workShift.getId(),
                now, null, status, employeeLocation, distance, null
        );

        record.registerEvent(EmployeeClockedInEvent.now(
                attendanceId, tenantId, branchId, membershipId, workShift.getId(), status, now
        ));

        if (status == AttendanceStatus.LATE) {
            record.registerEvent(LateAttendanceRecordedEvent.now(
                    attendanceId, tenantId, membershipId, delayMinutes
            ));
        }

        return record;
    }

    public void recordClockOut(Instant clockOutTime) {
        if (this.clockOut != null) {
            throw new InvalidAttendanceClockOutException("La salida ya ha sido registrada previamente para esta jornada");
        }
        Instant effectiveClockOut = clockOutTime != null ? clockOutTime : Instant.now();
        if (effectiveClockOut.isBefore(this.clockIn)) {
            throw new InvalidAttendanceClockOutException("La hora de salida no puede ser anterior a la hora de entrada");
        }

        this.clockOut = effectiveClockOut;
        long workedMinutes = Duration.between(this.clockIn, this.clockOut).toMinutes();

        registerEvent(EmployeeClockedOutEvent.now(
                this.id, this.tenantId, this.membershipId, this.clockOut, workedMinutes
        ));
    }

    public void justify(String reason, TenantMembershipId justifiedBy) {
        if (this.status == AttendanceStatus.ON_TIME) {
            throw new AttendanceNotJustifiableException("No es posible justificar una asistencia que fue puntual");
        }
        this.justification = AttendanceJustification.of(reason, justifiedBy, Instant.now());
        this.status = AttendanceStatus.EXCUSED;

        registerEvent(AttendanceJustifiedEvent.now(
                this.id, this.tenantId, this.membershipId, justifiedBy, reason
        ));
    }

    public long totalWorkedMinutes() {
        if (this.clockOut == null) return 0;
        return Duration.between(this.clockIn, this.clockOut).toMinutes();
    }

    public AttendanceRecordId getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public BranchId getBranchId() {
        return branchId;
    }

    public TenantMembershipId getMembershipId() {
        return membershipId;
    }

    public WorkShiftId getShiftId() {
        return shiftId;
    }

    public Instant getClockIn() {
        return clockIn;
    }

    public Instant getClockOut() {
        return clockOut;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public GeoCoordinates getCoordinates() {
        return coordinates;
    }

    public HaversineDistance getDistanceToBranch() {
        return distanceToBranch;
    }

    public Optional<AttendanceJustification> getJustification() {
        return Optional.ofNullable(justification);
    }
}
