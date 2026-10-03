package com.andeva.atelier.platform.crm.application.queryservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByCustomerQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByDateRangeQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByTenantAndBranchQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByVehicleQuery;

import java.util.List;
import java.util.Optional;

/**
 * Public application port for appointment read queries.
 *
 * @author Adiel Sanchez Santin
 */
public interface AppointmentQueryService {

    Optional<Appointment> handle(GetAppointmentByIdQuery query);

    List<Appointment> handle(GetAppointmentsByTenantAndBranchQuery query);

    List<Appointment> handle(GetAppointmentsByCustomerQuery query);

    List<Appointment> handle(GetAppointmentsByDateRangeQuery query);

    List<Appointment> handle(GetAppointmentsByVehicleQuery query);
}
