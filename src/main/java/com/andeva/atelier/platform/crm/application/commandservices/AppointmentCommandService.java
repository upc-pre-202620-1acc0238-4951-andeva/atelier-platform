package com.andeva.atelier.platform.crm.application.commandservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.commands.CancelAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ConfirmAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RescheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;

/**
 * Public application port for appointment scheduling commands.
 *
 * @author Adiel Sanchez Santin
 */
public interface AppointmentCommandService {

    Result<Appointment, ApplicationError> handle(ScheduleAppointmentCommand command);

    Result<Appointment, ApplicationError> handle(ConfirmAppointmentCommand command);

    Result<Appointment, ApplicationError> handle(MarkAppointmentArrivedCommand command);

    Result<Appointment, ApplicationError> handle(RescheduleAppointmentCommand command);

    Result<Appointment, ApplicationError> handle(CancelAppointmentCommand command);
}
