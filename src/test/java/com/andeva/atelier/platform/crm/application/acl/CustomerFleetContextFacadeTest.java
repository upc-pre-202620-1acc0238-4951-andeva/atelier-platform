package com.andeva.atelier.platform.crm.application.acl;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.entities.CustomerMembership;
import com.andeva.atelier.platform.crm.domain.model.entities.VehicleOwnership;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerMembershipStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.enums.FleetRole;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.ids.CustomerMembershipId;
import com.andeva.atelier.platform.crm.domain.model.ids.VehicleOwnershipId;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.crm.domain.repositories.AppointmentRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerMembershipRepository;
import com.andeva.atelier.platform.crm.domain.repositories.CustomerRepository;
import com.andeva.atelier.platform.crm.domain.repositories.VehicleRepository;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.AppointmentAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.CustomerMembershipAclDto;
import com.andeva.atelier.platform.crm.interfaces.acl.dto.VehicleAclDto;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit test suite verifying the CustomerFleetContextFacadeImpl Open Host Service (OHS).
 * Tests all 12 contract methods defined in canonical documentation.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerFleetContextFacadeImpl Unit Tests")
class CustomerFleetContextFacadeTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private CustomerMembershipRepository customerMembershipRepository;

    @Mock
    private AppointmentCommandService appointmentCommandService;

    private CustomerFleetContextFacadeImpl facade;

    private TenantId sampleTenantId;
    private CustomerId sampleCustomerId;
    private VehicleId sampleVehicleId;

    @BeforeEach
    void setUp() {
        facade = new CustomerFleetContextFacadeImpl(
                customerRepository,
                vehicleRepository,
                appointmentRepository,
                customerMembershipRepository,
                appointmentCommandService
        );
        sampleTenantId = TenantId.generate();
        sampleCustomerId = CustomerId.generate();
        sampleVehicleId = VehicleId.generate();
    }

    @Test
    @DisplayName("fetchCustomerById: should return mapped DTO when customer exists")
    void fetchCustomerById_ShouldReturnDto() {
        Customer customer = Customer.registerIndividual(
                sampleCustomerId,
                sampleTenantId,
                PersonName.of("Carlos", "Gomez"),
                TaxId.of("12345678"),
                EmailAddress.of("carlos@example.pe"),
                PhoneNumber.of("+51987654321")
        );
        when(customerRepository.findById(sampleCustomerId)).thenReturn(Optional.of(customer));

        Optional<CustomerAclDto> result = facade.fetchCustomerById(sampleCustomerId.value());

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(sampleCustomerId.value());
        assertThat(result.get().displayName()).isEqualTo("Carlos Gomez");
        assertThat(result.get().taxId()).isEqualTo("12345678");
        assertThat(result.get().type()).isEqualTo("INDIVIDUAL");
    }

    @Test
    @DisplayName("fetchCustomerById: should return empty when customerId is null or not found")
    void fetchCustomerById_ShouldReturnEmpty() {
        assertThat(facade.fetchCustomerById(null)).isEmpty();

        when(customerRepository.findById(any())).thenReturn(Optional.empty());
        assertThat(facade.fetchCustomerById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("fetchCustomerByTenantIdAndTaxId: should return mapped DTO when customer exists")
    void fetchCustomerByTenantIdAndTaxId_ShouldReturnDto() {
        Customer customer = Customer.registerCompany(
                sampleCustomerId,
                sampleTenantId,
                "Transportes Express SAC",
                TaxId.of("20100070970"),
                EmailAddress.of("contacto@transportes.pe"),
                PhoneNumber.of("+51987654321")
        );
        when(customerRepository.findByTenantIdAndTaxId(sampleTenantId, "20100070970"))
                .thenReturn(Optional.of(customer));

        Optional<CustomerAclDto> result = facade.fetchCustomerByTenantIdAndTaxId(sampleTenantId.value(), "20100070970");

        assertThat(result).isPresent();
        assertThat(result.get().displayName()).isEqualTo("Transportes Express SAC");
        assertThat(result.get().type()).isEqualTo("COMPANY");
    }

    @Test
    @DisplayName("fetchCustomerByTenantIdAndTaxId: should return empty when inputs are null or not found")
    void fetchCustomerByTenantIdAndTaxId_ShouldReturnEmpty() {
        assertThat(facade.fetchCustomerByTenantIdAndTaxId(null, "20100070970")).isEmpty();
        assertThat(facade.fetchCustomerByTenantIdAndTaxId(sampleTenantId.value(), null)).isEmpty();
    }

    @Test
    @DisplayName("fetchVehicleById: should return mapped DTO when vehicle exists")
    void fetchVehicleById_ShouldReturnDto() {
        Vehicle vehicle = Vehicle.register(
                sampleVehicleId,
                LicensePlate.of("ABC123"),
                Vin.of("1HGCR2F83HA000000"),
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );
        when(vehicleRepository.findById(sampleVehicleId)).thenReturn(Optional.of(vehicle));

        Optional<VehicleAclDto> result = facade.fetchVehicleById(sampleVehicleId.value());

        assertThat(result).isPresent();
        assertThat(result.get().plate()).isEqualTo("ABC123");
        assertThat(result.get().brand()).isEqualTo("Toyota");
        assertThat(result.get().model()).isEqualTo("Corolla");
        assertThat(result.get().year()).isEqualTo(2022);
    }

    @Test
    @DisplayName("fetchVehicleByPlate: should return vehicle DTO when plate matches")
    void fetchVehicleByPlate_ShouldReturnDto() {
        Vehicle vehicle = Vehicle.register(
                sampleVehicleId,
                LicensePlate.of("XYZ789"),
                Vin.of("1HGCR2F83HA000001"),
                "Nissan",
                "Sentra",
                2021,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );
        when(vehicleRepository.findByPlate(any(LicensePlate.class))).thenReturn(Optional.of(vehicle));

        Optional<VehicleAclDto> result = facade.fetchVehicleByPlate("XYZ-789");

        assertThat(result).isPresent();
        assertThat(result.get().brand()).isEqualTo("Nissan");
    }

    @Test
    @DisplayName("fetchVehicleByPlate: should return empty for invalid or null plate")
    void fetchVehicleByPlate_ShouldReturnEmpty() {
        assertThat(facade.fetchVehicleByPlate(null)).isEmpty();
        assertThat(facade.fetchVehicleByPlate("   ")).isEmpty();
        assertThat(facade.fetchVehicleByPlate("INVALID-PLATE-STRING")).isEmpty();
    }

    @Test
    @DisplayName("fetchCurrentOwnerId: should return customer UUID when active ownership exists")
    void fetchCurrentOwnerId_ShouldReturnOwner() {
        Vehicle vehicle = Vehicle.register(
                sampleVehicleId,
                LicensePlate.of("DEF456"),
                null,
                "Honda",
                "Civic",
                2020,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );
        when(vehicleRepository.findById(sampleVehicleId)).thenReturn(Optional.of(vehicle));

        Optional<UUID> ownerId = facade.fetchCurrentOwnerId(sampleVehicleId.value());

        assertThat(ownerId).contains(sampleCustomerId.value());
    }

    @Test
    @DisplayName("fetchCurrentOwnerId: should return empty when vehicleId is null or not found")
    void fetchCurrentOwnerId_ShouldReturnEmpty() {
        assertThat(facade.fetchCurrentOwnerId(null)).isEmpty();
        when(vehicleRepository.findById(sampleVehicleId)).thenReturn(Optional.empty());
        assertThat(facade.fetchCurrentOwnerId(sampleVehicleId.value())).isEmpty();
    }

    @Test
    @DisplayName("fetchVehiclesByCustomerId: should return active vehicles list")
    void fetchVehiclesByCustomerId_ShouldReturnList() {
        Vehicle vehicle = Vehicle.register(
                sampleVehicleId,
                LicensePlate.of("ABC123"),
                null,
                "Toyota",
                "Yaris",
                2019,
                EngineType.GASOLINE,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );
        when(vehicleRepository.findByCurrentOwnerId(sampleCustomerId)).thenReturn(List.of(vehicle));

        List<VehicleAclDto> list = facade.fetchVehiclesByCustomerId(sampleCustomerId.value());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).model()).isEqualTo("Yaris");
    }

    @Test
    @DisplayName("fetchVehiclesByCustomerId: should return empty list when customerId is null")
    void fetchVehiclesByCustomerId_ShouldReturnEmptyListForNull() {
        assertThat(facade.fetchVehiclesByCustomerId(null)).isEmpty();
    }

    @Test
    @DisplayName("fetchAppointmentById: should return mapped DTO when appointment exists")
    void fetchAppointmentById_ShouldReturnDto() {
        AppointmentId apptId = AppointmentId.generate();
        BranchId branchId = BranchId.generate();
        Instant scheduledAt = Instant.now().plusSeconds(3600);

        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                branchId,
                sampleCustomerId,
                sampleVehicleId,
                scheduledAt,
                60,
                "Mantenimiento regular 10,000 km"
        );
        when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));

        Optional<AppointmentAclDto> result = facade.fetchAppointmentById(apptId.value());

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(apptId.value());
        assertThat(result.get().status()).isEqualTo("PENDING");
        assertThat(result.get().estimatedDurationMinutes()).isEqualTo(60);
    }

    @Test
    @DisplayName("markAppointmentAsConvertedToWorkOrder: should succeed when appointment arrives")
    void markAppointmentAsConvertedToWorkOrder_ShouldSucceed() {
        AppointmentId apptId = AppointmentId.generate();
        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                BranchId.generate(),
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(3600),
                60,
                "Revisión"
        );
        when(appointmentRepository.findById(apptId)).thenReturn(Optional.of(appointment));
        when(appointmentCommandService.handle(any(MarkAppointmentArrivedCommand.class))).thenReturn(Result.success(appointment));

        boolean marked = facade.markAppointmentAsConvertedToWorkOrder(apptId.value());

        assertThat(marked).isTrue();
    }

    @Test
    @DisplayName("markAppointmentAsConvertedToWorkOrder: should return false when appointment is null or missing")
    void markAppointmentAsConvertedToWorkOrder_ShouldReturnFalse() {
        assertThat(facade.markAppointmentAsConvertedToWorkOrder(null)).isFalse();

        when(appointmentRepository.findById(any())).thenReturn(Optional.empty());
        assertThat(facade.markAppointmentAsConvertedToWorkOrder(UUID.randomUUID())).isFalse();
    }

    @Test
    @DisplayName("fetchActiveMembershipsByUserId: should return list of active fleet memberships")
    void fetchActiveMembershipsByUserId_ShouldReturnList() {
        UserId userId = UserId.generate();
        CustomerMembership membership = CustomerMembership.create(
                CustomerMembershipId.generate(),
                sampleCustomerId,
                userId,
                FleetRole.FLEET_ADMIN
        );
        when(customerMembershipRepository.findByUserIdAndStatus(userId, CustomerMembershipStatus.ACTIVE))
                .thenReturn(List.of(membership));

        List<CustomerMembershipAclDto> list = facade.fetchActiveMembershipsByUserId(userId.value());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).role()).isEqualTo("FLEET_ADMIN");
        assertThat(list.get(0).status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("hasFleetRole: should return true for FLEET_ADMIN on any required role")
    void hasFleetRole_FleetAdminShouldHaveAllRoles() {
        UserId userId = UserId.generate();
        CustomerMembership membership = CustomerMembership.create(
                CustomerMembershipId.generate(),
                sampleCustomerId,
                userId,
                FleetRole.FLEET_ADMIN
        );
        when(customerMembershipRepository.findByCustomerIdAndUserId(sampleCustomerId, userId))
                .thenReturn(Optional.of(membership));

        boolean hasRole = facade.hasFleetRole(sampleCustomerId.value(), userId.value(), "FLEET_OPERATOR");

        assertThat(hasRole).isTrue();
    }

    @Test
    @DisplayName("hasFleetRole: should return false when membership is inactive or not found")
    void hasFleetRole_ShouldReturnFalseWhenInactive() {
        UserId userId = UserId.generate();
        assertThat(facade.hasFleetRole(null, userId.value(), "FLEET_ADMIN")).isFalse();
        assertThat(facade.hasFleetRole(sampleCustomerId.value(), null, "FLEET_ADMIN")).isFalse();
        assertThat(facade.hasFleetRole(sampleCustomerId.value(), userId.value(), null)).isFalse();

        when(customerMembershipRepository.findByCustomerIdAndUserId(sampleCustomerId, userId))
                .thenReturn(Optional.empty());
        assertThat(facade.hasFleetRole(sampleCustomerId.value(), userId.value(), "FLEET_ADMIN")).isFalse();
    }

    @Test
    @DisplayName("fetchMembership: should return membership DTO when exists")
    void fetchMembership_ShouldReturnDto() {
        UserId userId = UserId.generate();
        CustomerMembership membership = CustomerMembership.create(
                CustomerMembershipId.generate(),
                sampleCustomerId,
                userId,
                FleetRole.FLEET_OPERATOR
        );
        when(customerMembershipRepository.findByCustomerIdAndUserId(sampleCustomerId, userId))
                .thenReturn(Optional.of(membership));

        Optional<CustomerMembershipAclDto> result = facade.fetchMembership(sampleCustomerId.value(), userId.value());

        assertThat(result).isPresent();
        assertThat(result.get().role()).isEqualTo("FLEET_OPERATOR");
    }

    @Test
    @DisplayName("schedulePreventiveAppointment: should schedule appointment and return generated ID")
    void schedulePreventiveAppointment_ShouldSucceed() {
        Vehicle vehicle = Vehicle.register(
                sampleVehicleId,
                LicensePlate.of("ABC123"),
                null,
                "Toyota",
                "Hilux",
                2022,
                EngineType.DIESEL,
                Optional.of(sampleCustomerId),
                Optional.empty()
        );
        when(vehicleRepository.findById(sampleVehicleId)).thenReturn(Optional.of(vehicle));

        AppointmentId apptId = AppointmentId.generate();
        Appointment appointment = Appointment.schedule(
                apptId,
                sampleTenantId,
                BranchId.generate(),
                sampleCustomerId,
                sampleVehicleId,
                Instant.now().plusSeconds(86400),
                60,
                "Cambio de aceite preventivo"
        );
        when(appointmentCommandService.handle(any(ScheduleAppointmentCommand.class))).thenReturn(Result.success(appointment));

        Optional<UUID> generatedId = facade.schedulePreventiveAppointment(
                sampleTenantId.value(),
                sampleVehicleId.value(),
                "Alerta preventiva OBD-II"
        );

        assertThat(generatedId).contains(apptId.value());
    }

    @Test
    @DisplayName("schedulePreventiveAppointment: should return empty when inputs are null or vehicle has no owner")
    void schedulePreventiveAppointment_ShouldReturnEmptyWhenNoOwner() {
        assertThat(facade.schedulePreventiveAppointment(null, sampleVehicleId.value(), "desc")).isEmpty();
        assertThat(facade.schedulePreventiveAppointment(sampleTenantId.value(), null, "desc")).isEmpty();

        when(vehicleRepository.findById(sampleVehicleId)).thenReturn(Optional.empty());
        assertThat(facade.schedulePreventiveAppointment(sampleTenantId.value(), sampleVehicleId.value(), "desc")).isEmpty();
    }
}
