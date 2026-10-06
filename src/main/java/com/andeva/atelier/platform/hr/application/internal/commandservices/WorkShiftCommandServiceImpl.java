package com.andeva.atelier.platform.hr.application.internal.commandservices;

import com.andeva.atelier.platform.hr.application.commandservices.WorkShiftCommandService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.WorkShift;
import com.andeva.atelier.platform.hr.domain.model.commands.ActivateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.CreateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.DeactivateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateWorkShiftCommand;
import com.andeva.atelier.platform.hr.domain.repositories.WorkShiftRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class WorkShiftCommandServiceImpl implements WorkShiftCommandService {

    private final WorkShiftRepository workShiftRepository;

    public WorkShiftCommandServiceImpl(WorkShiftRepository workShiftRepository) {
        this.workShiftRepository = Objects.requireNonNull(workShiftRepository, "workShiftRepository cannot be null");
    }

    @Override
    public Result<WorkShift, ApplicationError> handle(CreateWorkShiftCommand command) {
        if (workShiftRepository.existsByTenantIdAndName(command.tenantId(), command.name())) {
            return Result.failure(ApplicationError.conflict(
                    "Ya existe un turno de trabajo registrado con el nombre '" + command.name() + "' en este taller"
            ));
        }

        try {
            WorkShift shift = WorkShift.create(
                    command.tenantId(),
                    command.name(),
                    command.startTime(),
                    command.endTime(),
                    command.gracePeriodMinutes()
            );
            WorkShift saved = workShiftRepository.save(shift);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<WorkShift, ApplicationError> handle(UpdateWorkShiftCommand command) {
        Optional<WorkShift> existingOpt = workShiftRepository.findById(command.workShiftId());
        if (existingOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound(
                    "No se encontró el turno laboral con ID: " + command.workShiftId().value()
            ));
        }

        WorkShift shift = existingOpt.get();
        if (!shift.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("El turno pertenece a otro taller"));
        }

        try {
            shift.updateSchedule(command.name(), command.startTime(), command.endTime(), command.gracePeriodMinutes());
            WorkShift saved = workShiftRepository.save(shift);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<Void, ApplicationError> handle(ActivateWorkShiftCommand command) {
        Optional<WorkShift> existingOpt = workShiftRepository.findById(command.workShiftId());
        if (existingOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el turno laboral"));
        }

        WorkShift shift = existingOpt.get();
        if (!shift.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("El turno pertenece a otro taller"));
        }

        shift.activate();
        workShiftRepository.save(shift);
        return Result.success(null);
    }

    @Override
    public Result<Void, ApplicationError> handle(DeactivateWorkShiftCommand command) {
        Optional<WorkShift> existingOpt = workShiftRepository.findById(command.workShiftId());
        if (existingOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el turno laboral"));
        }

        WorkShift shift = existingOpt.get();
        if (!shift.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("El turno pertenece a otro taller"));
        }

        shift.deactivate();
        workShiftRepository.save(shift);
        return Result.success(null);
    }
}
