package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentAlreadyArrivedException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentInvalidStateTransitionException;
import com.andeva.atelier.platform.crm.domain.exceptions.AppointmentPastDateException;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.CancelAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ConfirmAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RescheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.domain.services.AppointmentSchedulingService;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.exceptions.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of AppointmentCommandService handling appointment state machine write operations.
 *
 * @author Adiel Sanchez Santin
 */
@Service
@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
public class AppointmentCommandServiceImpl implements AppointmentCommandService {

    private static final int DEFAULT_MAX_CONCURRENT_SLOTS = 5;

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final AppointmentSchedulingService appointmentSchedulingService;

    public AppointmentCommandServiceImpl(
            AppointmentRepository appointmentRepository,
            CustomerRepository customerRepository,
            VehicleRepository vehicleRepository,
            AppointmentSchedulingService appointmentSchedulingService
    ) {
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "AppointmentRepository cannot be null");
        this.customerRepository = Objects.requireNonNull(customerRepository, "CustomerRepository cannot be null");
        this.vehicleRepository = Objects.requireNonNull(vehicleRepository, "VehicleRepository cannot be null");
        this.appointmentSchedulingService = Objects.requireNonNull(appointmentSchedulingService, "AppointmentSchedulingService cannot be null");
    }

    @Override
    public Result<Appointment, ApplicationError> handle(ScheduleAppointmentCommand command) {
        Objects.requireNonNull(command, "ScheduleAppointmentCommand cannot be null");

        Optional<Customer> customerOpt = customerRepository.findByIdAndTenantId(command.customerId(), command.tenantId());
        if (customerOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Customer", command.customerId().value()));
        }

        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(command.vehicleId());
        if (vehicleOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Vehicle", command.vehicleId().value()));
        }

        Result<Void, DomainException> slotResult = appointmentSchedulingService.validateSlotAvailability(
                command.tenantId(),
                command.branchId(),
                command.scheduledAt(),
                command.estimatedDurationMinutes(),
                DEFAULT_MAX_CONCURRENT_SLOTS
        );

        if (slotResult instanceof Result.Failure<Void, DomainException> failure) {
            return Result.failure(ApplicationError.badRequest(failure.error().getMessage()));
        }

        Appointment appointment;
        try {
            appointment = Appointment.schedule(
                    AppointmentId.generate(),
                    command.tenantId(),
                    command.branchId(),
                    command.customerId(),
                    command.vehicleId(),
                    command.scheduledAt(),
                    command.estimatedDurationMinutes(),
                    command.reason()
            );
        } catch (AppointmentPastDateException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        Appointment saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }

    @Override
    public Result<Appointment, ApplicationError> handle(ConfirmAppointmentCommand command) {
        Objects.requireNonNull(command, "ConfirmAppointmentCommand cannot be null");

        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndTenantId(command.appointmentId(), command.tenantId());
        if (appointmentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Appointment", command.appointmentId().value()));
        }

        Appointment appointment = appointmentOpt.get();
        try {
            appointment.confirm();
        } catch (AppointmentInvalidStateTransitionException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        Appointment saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }

    @Override
    public Result<Appointment, ApplicationError> handle(MarkAppointmentArrivedCommand command) {
        Objects.requireNonNull(command, "MarkAppointmentArrivedCommand cannot be null");

        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndTenantId(command.appointmentId(), command.tenantId());
        if (appointmentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Appointment", command.appointmentId().value()));
        }

        Appointment appointment = appointmentOpt.get();
        try {
            appointment.markArrived();
        } catch (AppointmentAlreadyArrivedException | AppointmentInvalidStateTransitionException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        Appointment saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }

    @Override
    public Result<Appointment, ApplicationError> handle(RescheduleAppointmentCommand command) {
        Objects.requireNonNull(command, "RescheduleAppointmentCommand cannot be null");

        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndTenantId(command.appointmentId(), command.tenantId());
        if (appointmentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Appointment", command.appointmentId().value()));
        }

        Appointment appointment = appointmentOpt.get();

        Result<Void, DomainException> slotResult = appointmentSchedulingService.validateSlotAvailability(
                command.tenantId(),
                appointment.branchId(),
                command.newScheduledAt(),
                appointment.estimatedDurationMinutes(),
                DEFAULT_MAX_CONCURRENT_SLOTS
        );

        if (slotResult instanceof Result.Failure<Void, DomainException> failure) {
            return Result.failure(ApplicationError.badRequest(failure.error().getMessage()));
        }

        try {
            appointment.reschedule(command.newScheduledAt());
        } catch (AppointmentPastDateException | AppointmentInvalidStateTransitionException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        Appointment saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }

    @Override
    public Result<Appointment, ApplicationError> handle(CancelAppointmentCommand command) {
        Objects.requireNonNull(command, "CancelAppointmentCommand cannot be null");

        Optional<Appointment> appointmentOpt = appointmentRepository.findByIdAndTenantId(command.appointmentId(), command.tenantId());
        if (appointmentOpt.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Appointment", command.appointmentId().value()));
        }

        Appointment appointment = appointmentOpt.get();
        try {
            appointment.cancel(command.cancellationReason());
        } catch (AppointmentInvalidStateTransitionException e) {
            return Result.failure(ApplicationError.badRequest(e.getMessage()));
        }

        Appointment saved = appointmentRepository.save(appointment);
        return Result.success(saved);
    }
}
