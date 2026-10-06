package com.andeva.atelier.platform.hr.application.internal.commandservices;

import com.andeva.atelier.platform.hr.application.commandservices.AttendanceCommandService;
import com.andeva.atelier.platform.hr.application.internal.outbound.acl.TenancyGeofenceAclService;
import com.andeva.atelier.platform.hr.domain.exceptions.GeofenceViolationException;
import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.JustifyAttendanceCommand;
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

import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class AttendanceCommandServiceImpl implements AttendanceCommandService {

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
                    ZoneId.of("America/Lima")
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
            record.recordClockOut(command.clockOutTime());
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
}
