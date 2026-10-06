package com.andeva.atelier.platform.hr.application.internal.commandservices;

import com.andeva.atelier.platform.hr.application.commandservices.EmployeeProfileCommandService;
import com.andeva.atelier.platform.hr.application.internal.outbound.acl.TenancyGeofenceAclService;
import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.commands.AssignShiftToEmployeeCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RegisterEmployeeProfileCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateEmploymentStatusCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateSalaryCommand;
import com.andeva.atelier.platform.hr.domain.repositories.EmployeeProfileRepository;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class EmployeeProfileCommandServiceImpl implements EmployeeProfileCommandService {

    private final EmployeeProfileRepository employeeRepository;
    private final TenancyGeofenceAclService tenancyAclService;

    public EmployeeProfileCommandServiceImpl(
            EmployeeProfileRepository employeeRepository,
            TenancyGeofenceAclService tenancyAclService
    ) {
        this.employeeRepository = Objects.requireNonNull(employeeRepository, "employeeRepository cannot be null");
        this.tenancyAclService = Objects.requireNonNull(tenancyAclService, "tenancyAclService cannot be null");
    }

    @Override
    public Result<EmployeeProfile, ApplicationError> handle(RegisterEmployeeProfileCommand command) {
        if (employeeRepository.existsByMembershipId(command.membershipId())) {
            return Result.failure(ApplicationError.conflict("El colaborador ya cuenta con expediente laboral registrado"));
        }

        if (!tenancyAclService.isValidActiveMembership(command.tenantId(), command.membershipId())) {
            return Result.failure(ApplicationError.badRequest("La membresía de IAM no es válida o está inactiva"));
        }

        try {
            EmployeeProfile profile = EmployeeProfile.register(
                    command.tenantId(),
                    command.branchId(),
                    command.membershipId(),
                    command.shiftId(),
                    command.baseSalary(),
                    command.compensationType(),
                    command.jobTitle()
            );

            EmployeeProfile saved = employeeRepository.save(profile);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<EmployeeProfile, ApplicationError> handle(AssignShiftToEmployeeCommand command) {
        Optional<EmployeeProfile> profileOpt = employeeRepository.findById(command.profileId());
        if (profileOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el expediente del empleado"));
        }

        EmployeeProfile profile = profileOpt.get();
        if (!profile.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("El expediente pertenece a otro taller"));
        }

        try {
            profile.assignShift(command.shiftId());
            EmployeeProfile saved = employeeRepository.save(profile);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<EmployeeProfile, ApplicationError> handle(UpdateSalaryCommand command) {
        Optional<EmployeeProfile> profileOpt = employeeRepository.findById(command.profileId());
        if (profileOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el expediente del empleado"));
        }

        EmployeeProfile profile = profileOpt.get();
        if (!profile.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("El expediente pertenece a otro taller"));
        }

        try {
            profile.updateSalary(command.newSalary(), command.compensationType());
            EmployeeProfile saved = employeeRepository.save(profile);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }

    @Override
    public Result<EmployeeProfile, ApplicationError> handle(UpdateEmploymentStatusCommand command) {
        Optional<EmployeeProfile> profileOpt = employeeRepository.findById(command.profileId());
        if (profileOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("No se encontró el expediente del empleado"));
        }

        EmployeeProfile profile = profileOpt.get();
        if (!profile.getTenantId().equals(command.tenantId())) {
            return Result.failure(ApplicationError.forbidden("El expediente pertenece a otro taller"));
        }

        try {
            profile.updateStatus(command.newStatus());
            EmployeeProfile saved = employeeRepository.save(profile);
            return Result.success(saved);
        } catch (Exception ex) {
            return Result.failure(ApplicationError.badRequest(ex.getMessage()));
        }
    }
}
