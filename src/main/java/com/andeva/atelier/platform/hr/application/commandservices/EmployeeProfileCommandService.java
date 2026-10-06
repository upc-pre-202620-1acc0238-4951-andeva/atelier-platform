package com.andeva.atelier.platform.hr.application.commandservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.EmployeeProfile;
import com.andeva.atelier.platform.hr.domain.model.commands.AssignShiftToEmployeeCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RegisterEmployeeProfileCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateEmploymentStatusCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.UpdateSalaryCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface EmployeeProfileCommandService {
    Result<EmployeeProfile, ApplicationError> handle(RegisterEmployeeProfileCommand command);
    Result<EmployeeProfile, ApplicationError> handle(AssignShiftToEmployeeCommand command);
    Result<EmployeeProfile, ApplicationError> handle(UpdateSalaryCommand command);
    Result<EmployeeProfile, ApplicationError> handle(UpdateEmploymentStatusCommand command);
}
