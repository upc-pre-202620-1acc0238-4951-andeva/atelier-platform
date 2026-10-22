package com.andeva.atelier.platform.hr.application.internal.commandservices;

import com.andeva.atelier.platform.hr.application.commandservices.AttendanceCommandService;
import com.andeva.atelier.platform.hr.application.internal.outbound.acl.TenancyGeofenceAclService;
import com.andeva.atelier.platform.hr.domain.exceptions.GeofenceViolationException;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.JustifyAttendanceCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.ReviewAttendanceJustificationCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.SubmitAttendanceJustificationCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockInCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockOutCommand;
import com.andeva.atelier.platform.hr.domain.repositories.AttendanceRecordRepository;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import com.andeva.atelier.platform.hr.domain.services.HaversineGeofencingService;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import com.andeva.atelier.platform.hr.domain.exceptions.AttendanceNotJustifiableException;
import com.andeva.atelier.platform.hr.domain.model.ids.AttendanceRecordId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class AttendanceCommandServiceImpl implements AttendanceCommandService {

    private static final ZoneId WORKSHOP_ZONE = ZoneId.of("America/Lima");
    /** Oldest clock-in captured offline that is still accepted. */
    private static final Duration MAX_OFFLINE_CLOCK_IN_AGE = Duration.ofHours(12);
    /** Device clocks may run slightly ahead of the server. */
    private static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofMinutes(2);

    private final AttendanceRecordRepository attendanceRepository;
    private final WorkShiftRepository workShiftRepository;
    private final TenancyGeofenceAclService geofenceAclService;
    private final HaversineGeofencingService geofencingService;

    public AttendanceCommandServiceImpl(
            AttendanceRecordRepository attendanceRepository,
            WorkShiftRepository workShiftRepository,
            TenancyGeofenceAclService geofenceAclService,
            HaversineGeofencingService geofencingService
    ) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository cannot be null");
        this.workShiftRepository = Objects.requireNonNull(workShiftRepository, "workShiftRepository cannot be null");
        this.geofenceAclService = Objects.requireNonNull(geofenceAclService, "geofenceAclService cannot be null");
        this.geofencingService = Objects.requireNonNull(geofencingService, "geofencingService cannot be null");
    }

    @Override
    public Result<AttendanceRecord, ApplicationError> handle(RecordClockInCommand command) {
        if (attendanceRepository.hasActiveClockIn(command.tenantId(), command.membershipId())) {
            return Result.failure(ApplicationError.conflict(
                    "El colaborador ya cuenta con una jornada de asistencia activa sin registrar salida"
            ));
        }

        Optional<WorkShift> shiftOpt = workShiftRepository.findById(command.shiftId());
        if (shiftOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el turno laboral programado"));
        }
        WorkShift workShift = shiftOpt.get();

        var branchGeofenceOpt = geofenceAclService.getBranchGeofence(command.tenantId(), command.branchId());
        if (branchGeofenceOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la sede física del taller"));
        }
        var branchGeofence = branchGeofenceOpt.get();

        Instant now = Instant.now();
        Instant clockInTime = command.clockInTime();
        if (clockInTime != null) {
            if (clockInTime.isAfter(now.plus(CLOCK_SKEW_TOLERANCE))) {
                return Result.failure(ApplicationError.badRequest("La hora de marcación no puede estar en el futuro"));
            }
            if (clockInTime.isBefore(now.minus(MAX_OFFLINE_CLOCK_IN_AGE))) {
                return Result.failure(ApplicationError.badRequest(
                        "La marcación sin conexión tiene más de 12 horas y ya no puede registrarse"));
            }
        }

        try {
            AttendanceRecord record = AttendanceRecord.recordClockIn(
                    command.tenantId(),
                    command.branchId(),
                    command.membershipId(),
                    workShift,
                    command.coordinates(),
                    branchGeofence.centroid(),
                    branchGeofence.allowedRadiusMeters(),
                    geofencingService,
                    WORKSHOP_ZONE,
                    clockInTime
            );

            AttendanceRecord saved = attendanceRepository.save(record);
            return Result.success(saved);
        } catch (GeofenceViolationException gve) {
            return Result.failure(ApplicationError.unprocessableEntity(gve.getMessage()));
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<AttendanceRecord, ApplicationError> handle(RecordClockOutCommand command) {
        Optional<AttendanceRecord> recordOpt;
        if (command.attendanceRecordId() != null) {
            recordOpt = attendanceRepository.findById(command.attendanceRecordId());
        } else {
            recordOpt = attendanceRepository.findActiveByMembership(command.tenantId(), command.membershipId());
        }

        if (recordOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la marcación de asistencia"));
        }

        AttendanceRecord record = recordOpt.get();
        if (!record.getTenantId().equals(command.tenantId()) || !record.getMembershipId().equals(command.membershipId())) {
            return Result.failure(ApplicationError.forbidden("La marcación no corresponde al colaborador"));
        }

        try {
            WorkShift shift = workShiftRepository.findById(record.getShiftId()).orElse(null);
            record.recordClockOut(command.clockOutTime(), shift, WORKSHOP_ZONE);
            AttendanceRecord saved = attendanceRepository.save(record);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<AttendanceRecord, ApplicationError> handle(JustifyAttendanceCommand command) {
        Optional<AttendanceRecord> recordOpt = attendanceRepository.findById(command.attendanceRecordId());
        if (recordOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró la marcación de asistencia"));
        }

        AttendanceRecord record = recordOpt.get();
        if (!record.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("La marcación no corresponde a este taller"));
        }

        try {
            record.justify(command.reason(), command.supervisorMembershipId());
            AttendanceRecord saved = attendanceRepository.save(record);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<AttendanceRecord, ApplicationError> handle(SubmitAttendanceJustificationCommand command) {
        return changeRecord(command.tenantId(), command.attendanceRecordId(),
                record -> record.submitJustification(command.reason(), command.employeeMembershipId()));
    }

    @Override
    public Result<AttendanceRecord, ApplicationError> handle(ReviewAttendanceJustificationCommand command) {
        return changeRecord(command.tenantId(), command.attendanceRecordId(), record -> {
            if (command.approved()) {
                record.approveJustification(command.reviewerMembershipId());
            } else {
                record.rejectJustification(command.reviewerMembershipId());
            }
        });
    }

    private Result<AttendanceRecord, ApplicationError> changeRecord(TenantId tenantId, AttendanceRecordId recordId,
                                                                    java.util.function.Consumer<AttendanceRecord> change) {
        Optional<AttendanceRecord> recordOpt = attendanceRepository.findById(recordId);
        if (recordOpt.isEmpty() || !recordOpt.get().getTenantId().equals(tenantId)) {
            return Result.failure(ApplicationError.notFound("No se encontró la marcación de asistencia"));
        }
        AttendanceRecord record = recordOpt.get();
        try {
            change.accept(record);
            return Result.success(attendanceRepository.save(record));
        } catch (AttendanceNotJustifiableException ex) {
            return Result.failure(ApplicationError.unprocessableEntity(ex.getMessage()));
        }
    }
}
