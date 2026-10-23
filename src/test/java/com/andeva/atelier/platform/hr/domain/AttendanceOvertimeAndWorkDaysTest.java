package com.andeva.atelier.platform.hr.domain;

import com.andeva.atelier.platform.hr.application.internal.commandservices.AttendanceCommandServiceImpl;
import com.andeva.atelier.platform.hr.application.internal.outbound.acl.EmployeeNotificationPort;
import com.andeva.atelier.platform.hr.application.internal.outbound.acl.TenancyGeofenceAclService;
import com.andeva.atelier.platform.hr.application.internal.schedulers.ShiftStartReminderScheduler;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockInCommand;
import com.andeva.atelier.platform.hr.domain.model.events.OvertimeRecordedEvent;
import com.andeva.atelier.platform.hr.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.hr.domain.repositories.AttendanceRecordRepository;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import com.andeva.atelier.platform.hr.domain.services.HaversineGeofencingService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantMembershipId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Worked time, overtime, offline clock-in and rest days (US06, US08, US09)")
class AttendanceOvertimeAndWorkDaysTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final TenantId tenantId = TenantId.of(UUID.randomUUID());
    private final GeoCoordinates branch = GeoCoordinates.of(-12.0464, -77.0428);
    private final WorkShift shift = WorkShift.create(tenantId, "Mañana", LocalTime.of(8, 0), LocalTime.of(17, 0), 15);

    private AttendanceRecord clockedInAt(LocalTime time) {
        Instant clockIn = LocalDate.now(LIMA).atTime(time).atZone(LIMA).toInstant();
        return AttendanceRecord.recordClockIn(tenantId, BranchId.of(UUID.randomUUID()), TenantMembershipId.of(UUID.randomUUID()),
                shift, branch, branch, 100, new HaversineGeofencingService(), LIMA, clockIn);
    }

    private Instant today(LocalTime time) {
        return LocalDate.now(LIMA).atTime(time).atZone(LIMA).toInstant();
    }

    @Test
    @DisplayName("Leaving within 30 minutes of the shift end is ordinary time")
    void shouldNotCountOvertimeWithinTolerance() {
        AttendanceRecord record = clockedInAt(LocalTime.of(8, 0));
        record.recordClockOut(today(LocalTime.of(17, 20)), shift, LIMA);

        assertThat(record.getOvertimeMinutes()).isZero();
        assertThat(record.totalWorkedMinutes()).isEqualTo(560);
        assertThat(record.domainEvents()).noneMatch(e -> e instanceof OvertimeRecordedEvent);
    }

    @Test
    @DisplayName("Leaving more than 30 minutes late splits regular time and overtime and flags it for payroll")
    void shouldRecordOvertimePastTolerance() {
        AttendanceRecord record = clockedInAt(LocalTime.of(8, 0));
        record.recordClockOut(today(LocalTime.of(18, 15)), shift, LIMA);

        assertThat(record.getOvertimeMinutes()).isEqualTo(75);
        assertThat(record.regularWorkedMinutes()).isEqualTo(540);
        assertThat(record.domainEvents()).anyMatch(e -> e instanceof OvertimeRecordedEvent o && o.overtimeMinutes() == 75);
    }

    @Test
    @DisplayName("A clock-in queued offline keeps the device time, so lateness is judged at that moment")
    void shouldUseDeviceClockInTime() {
        AttendanceRecord record = clockedInAt(LocalTime.of(8, 5));

        assertThat(record.getClockIn()).isEqualTo(today(LocalTime.of(8, 5)));
        assertThat(record.getStatus().name()).isEqualTo("ON_TIME");
    }

    @Test
    @DisplayName("Offline clock-ins older than 12 hours or in the future are rejected")
    void shouldRejectImplausibleDeviceTimes() {
        AttendanceRecordRepository attendance = mock(AttendanceRecordRepository.class);
        WorkShiftRepository shifts = mock(WorkShiftRepository.class);
        TenancyGeofenceAclService geofence = mock(TenancyGeofenceAclService.class);
        when(shifts.findById(any())).thenReturn(Optional.of(shift));
        when(geofence.getBranchGeofence(any(), any()))
                .thenReturn(Optional.of(new TenancyGeofenceAclService.BranchGeofenceDto(branch, 100)));
        var service = new AttendanceCommandServiceImpl(attendance, shifts, geofence, new HaversineGeofencingService());

        for (Instant deviceTime : List.of(Instant.now().minus(Duration.ofHours(13)), Instant.now().plus(Duration.ofMinutes(10)))) {
            var result = service.handle(new RecordClockInCommand(tenantId, BranchId.of(UUID.randomUUID()),
                    TenantMembershipId.of(UUID.randomUUID()), shift.getId(), branch, deviceTime));
            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().code()).isEqualTo("BAD_REQUEST");
        }
        verify(attendance, never()).save(any());
    }

    @Test
    @DisplayName("A shift worked Monday to Saturday rests on Sunday")
    void shouldExposeRestDays() {
        WorkShift weekdays = WorkShift.create(tenantId, "Semana", LocalTime.of(8, 0), LocalTime.of(17, 0), 15,
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));

        assertThat(weekdays.isWorkDay(DayOfWeek.SUNDAY)).isFalse();
        assertThat(weekdays.getRestDays()).containsExactly(DayOfWeek.SUNDAY);
        assertThat(shift.getWorkDays()).hasSize(7);
        assertThat(shift.getRestDays()).isEmpty();
    }

    @Test
    @DisplayName("No shift reminder is sent on a rest day")
    void shouldSkipRemindersOnRestDays() {
        WorkShift weekdays = WorkShift.create(tenantId, "Semana", LocalTime.of(8, 0), LocalTime.of(17, 0), 15,
                EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));
        WorkShiftRepository shifts = mock(WorkShiftRepository.class);
        EmployeeProfileRepository employees = mock(EmployeeProfileRepository.class);
        EmployeeNotificationPort notifications = mock(EmployeeNotificationPort.class);
        when(shifts.findActiveStartingBetween(any(), any())).thenReturn(List.of(weekdays));
        var scheduler = new ShiftStartReminderScheduler(shifts, employees, notifications);

        assertThat(scheduler.remindShiftsStartingAfter(LocalTime.of(7, 45), DayOfWeek.SUNDAY)).isZero();
        verify(employees, never()).findAllActiveByAssignedShiftId(any());
    }
}
