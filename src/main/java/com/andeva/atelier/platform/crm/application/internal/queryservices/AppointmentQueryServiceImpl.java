package com.andeva.atelier.platform.crm.application.internal.queryservices;

import com.andeva.atelier.platform.crm.application.queryservices.AppointmentQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByCustomerQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByDateRangeQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByTenantAndBranchQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByVehicleQuery;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of AppointmentQueryService executing read-only projections.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(readOnly = true)
public class AppointmentQueryServiceImpl implements AppointmentQueryService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentQueryServiceImpl(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "AppointmentRepository cannot be null");
    }

    @Override
    public Optional<Appointment> handle(GetAppointmentByIdQuery query) {
        Objects.requireNonNull(query, "GetAppointmentByIdQuery cannot be null");
        return appointmentRepository.findByIdAndTenantId(query.appointmentId(), query.tenantId());
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByTenantAndBranchQuery query) {
        Objects.requireNonNull(query, "GetAppointmentsByTenantAndBranchQuery cannot be null");
        Instant startOfDay = query.date().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = query.date().atTime(23, 59, 59, 999999999).atZone(ZoneOffset.UTC).toInstant();

        List<Appointment> appointments = appointmentRepository.findByTenantIdAndBranchIdAndDate(
                query.tenantId(),
                query.branchId(),
                startOfDay,
                endOfDay
        );

        if (query.status() != null) {
            return appointments.stream()
                    .filter(a -> a.status() == query.status())
                    .toList();
        }
        return appointments;
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByCustomerQuery query) {
        Objects.requireNonNull(query, "GetAppointmentsByCustomerQuery cannot be null");
        return appointmentRepository.findByCustomerId(query.customerId());
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByDateRangeQuery query) {
        Objects.requireNonNull(query, "GetAppointmentsByDateRangeQuery cannot be null");
        return appointmentRepository.findByTenantIdAndBranchIdAndDate(
                query.tenantId(),
                query.branchId(),
                query.startDate(),
                query.endDate()
        );
    }

    @Override
    public List<Appointment> handle(GetAppointmentsByVehicleQuery query) {
        Objects.requireNonNull(query, "GetAppointmentsByVehicleQuery cannot be null");
        return appointmentRepository.findByVehicleId(query.vehicleId());
    }
}
