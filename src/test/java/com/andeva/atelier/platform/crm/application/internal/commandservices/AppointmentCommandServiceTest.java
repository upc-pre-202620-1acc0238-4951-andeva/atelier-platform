package com.andeva.atelier.platform.crm.application.internal.commandservices;

import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.CancelAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ConfirmAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RescheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.domain.services.AppointmentSchedulingService;
import com.andeva.atelier.platform.shared.application.result.ApplicationError;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test suite verifying {@link AppointmentCommandServiceImpl}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentCommandServiceImpl Unit Tests")
class AppointmentCommandServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    private AppointmentSchedulingService appointmentSchedulingService;

    private AppointmentCommandServiceImpl service;

    private TenantId sampleTenantId;
    private BranchId sampleBranchId;
    private CustomerId sampleCustomerId;
    private VehicleId sampleVehicleId;

    @BeforeEach
    void setUp() {
        appointmentSchedulingService = new AppointmentSchedulingService(appointmentRepository);
        service = new AppointmentCommandServiceImpl(
                appointmentRepository,
                customerRepository,
                vehicleRepository,
                appointmentSchedulingService
        );
        sampleTenantId = TenantId.generate();
        sampleBranchId = BranchId.generate();
        sampleCustomerId = CustomerId.generate();
        sampleVehicleId = VehicleId.generate();
    }

    @Test
    @DisplayName("Should successfully schedule appointment when data and slot are available")
    void shouldScheduleAppointmentSuccessfully() {
        Instant scheduledAt = Instant.now().plusSeconds(86400); // 24 hours in the future
        ScheduleAppointmentCommand command = new ScheduleAppointmentCommand(
                sampleTenantId,
                sampleBranchId,
                sampleCustomerId,
                sampleVehicleId,
                scheduledAt,
                60,
                "Alineamiento y balanceo"
        );

        Customer customer = Customer.registerIndividual(
                sampleCustomerId,
                sampleTenantId,
                PersonName.of("Carlos", "Rios"),
                TaxId.of("45871234"),
                EmailAddress.of("carlos.rios@example.pe"),
                PhoneNumber.of("+51987654321")
        );
        Vehicle vehicle = Vehicle.register(
                sampleVehicleId,
                LicensePlate.of("ABC123"),
                null,
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );

        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.of(customer));
        when(vehicleRepository.findById(sampleVehicleId)).thenReturn(Optional.of(vehicle));
        when(appointmentRepository.findActiveByBranchInWindow(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Result<Appointment, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        Appointment saved = result.toOptional().orElseThrow();
        assertThat(saved.status()).isEqualTo(AppointmentStatus.PENDING);
        assertThat(saved.reason()).isEqualTo("Alineamiento y balanceo");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Should reject scheduling when customer is not found")
    void shouldRejectSchedulingWhenCustomerNotFound() {
        ScheduleAppointmentCommand command = new ScheduleAppointmentCommand(
                sampleTenantId,
                sampleBranchId,
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(86400),
                60,
                "Revisión"
        );

        when(customerRepository.findByIdAndTenantId(sampleCustomerId, sampleTenantId))
                .thenReturn(Optional.empty());

        Result<Appointment, ApplicationError> result = service.handle(command);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().message()).contains("not found");
    }

    @Test
    @DisplayName("Should confirm pending appointment successfully")
    void shouldConfirmAppointmentSuccessfully() {
        AppointmentId apptId = AppointmentId.generate();
        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                sampleBranchId,
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(86400),
                60,
                "Revisión"
        );

        when(appointmentRepository.findByIdAndTenantId(apptId, sampleTenantId))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        ConfirmAppointmentCommand command = new ConfirmAppointmentCommand(sampleTenantId, apptId);
        Result<Appointment, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should mark appointment as arrived successfully")
    void shouldMarkAppointmentArrivedSuccessfully() {
        AppointmentId apptId = AppointmentId.generate();
        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                sampleBranchId,
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(86400),
                60,
                "Revisión"
        );
        appointment.confirm();

        when(appointmentRepository.findByIdAndTenantId(apptId, sampleTenantId))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        MarkAppointmentArrivedCommand command = new MarkAppointmentArrivedCommand(sampleTenantId, apptId);
        Result<Appointment, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.ARRIVED);
    }

    @Test
    @DisplayName("Should reschedule appointment to new future date")
    void shouldRescheduleAppointmentSuccessfully() {
        AppointmentId apptId = AppointmentId.generate();
        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                sampleBranchId,
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(86400),
                60,
                "Revisión"
        );

        Instant newDate = Instant.now().plusSeconds(172800); // 48 hours in future
        when(appointmentRepository.findByIdAndTenantId(apptId, sampleTenantId))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.findActiveByBranchInWindow(any(), any(), any(), any()))
                .thenReturn(List.of());
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        RescheduleAppointmentCommand command = new RescheduleAppointmentCommand(sampleTenantId, apptId, newDate);
        Result<Appointment, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(appointment.scheduledAt()).isEqualTo(newDate);
    }

    @Test
    @DisplayName("Should cancel appointment with justification reason")
    void shouldCancelAppointmentSuccessfully() {
        AppointmentId apptId = AppointmentId.generate();
        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                sampleBranchId,
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(86400),
                60,
                "Revisión"
        );

        when(appointmentRepository.findByIdAndTenantId(apptId, sampleTenantId))
                .thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(appointment)).thenReturn(appointment);

        CancelAppointmentCommand command = new CancelAppointmentCommand(
                sampleTenantId,
                apptId,
                "Cliente no podrá asistir por viaje de trabajo"
        );
        Result<Appointment, ApplicationError> result = service.handle(command);

        assertThat(result.isSuccess()).isTrue();
        assertThat(appointment.status()).isEqualTo(AppointmentStatus.CANCELED);
        assertThat(appointment.cancellationReason()).isEqualTo("Cliente no podrá asistir por viaje de trabajo");
    }
}
