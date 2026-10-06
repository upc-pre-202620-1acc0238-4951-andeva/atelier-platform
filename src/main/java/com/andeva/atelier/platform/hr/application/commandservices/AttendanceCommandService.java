package com.andeva.atelier.platform.hr.application.commandservices;

import com.andeva.atelier.platform.hr.domain.model.aggregates.AttendanceRecord;
import com.andeva.atelier.platform.hr.domain.model.commands.JustifyAttendanceCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockInCommand;
import com.andeva.atelier.platform.hr.domain.model.commands.RecordClockOutCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

public interface AttendanceCommandService {
    Result<AttendanceRecord, ApplicationError> handle(RecordClockInCommand command);
    Result<AttendanceRecord, ApplicationError> handle(RecordClockOutCommand command);
    Result<AttendanceRecord, ApplicationError> handle(JustifyAttendanceCommand command);
}
