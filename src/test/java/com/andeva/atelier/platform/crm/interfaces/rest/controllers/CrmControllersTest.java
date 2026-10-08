package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerMembershipCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.AppointmentQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.CustomerMembershipQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.commands.CancelAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ConfirmAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RescheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.InviteCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.RevokeCustomerMemberCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.TransferVehicleOwnershipCommand;
import com.andeva.atelier.platform.crm.domain.model.commands.UpdateCustomerContactCommand;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentsByTenantAndBranchQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerMembersByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomersByTenantIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByPlateQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleOwnershipHistoryQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByCustomerIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehiclesByUserIdQuery;
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
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CancelAppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateCompanyCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateIndividualCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateVehicleResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.InviteCustomerMemberResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.RescheduleAppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.ScheduleAppointmentResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.TransferVehicleOwnershipResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.UpdateCustomerContactResource;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Comprehensive Unit Tests for CRM & Fleet REST Controllers
 * covering the 22 canonical endpoints defined in docs/backend-documentation/api-endpoints/02-crm-and-fleet.md
 * and docs/backend-documentation/extended-description-bounded-contexts-backend/03-crm-and-fleet.md.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CRM REST Controllers Canonical Unit Tests")
class CrmControllersTest {

    @Mock
    private CustomerCommandService customerCommandService;
    @Mock
    private CustomerQueryService customerQueryService;
    @Mock
    private VehicleCommandService vehicleCommandService;
    @Mock
    private VehicleQueryService vehicleQueryService;
    @Mock
    private AppointmentCommandService appointmentCommandService;
    @Mock
    private AppointmentQueryService appointmentQueryService;
    @Mock
    private CustomerMembershipCommandService customerMembershipCommandService;
    @Mock
    private CustomerMembershipQueryService customerMembershipQueryService;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final CustomUserDetails testUser = new CustomUserDetails(
            userId,
            "advisor@atelier.pe",
            "password",
            tenantUuid,
            List.of(),
            true
    );

    private Customer sampleCustomer;
    private Vehicle sampleVehicle;
    private Appointment sampleAppointment;

    @BeforeEach
    void initSampleData() {
        sampleCustomer = Customer.registerIndividual(
                CustomerId.generate(),
                TenantId.of(tenantUuid),
                PersonName.of("Juan", "Perez"),
                TaxId.of("45871234"),
                EmailAddress.of("juan.perez@example.pe"),
                PhoneNumber.of("+51987654321")
        );

        sampleVehicle = Vehicle.register(
                VehicleId.generate(),
                LicensePlate.of("ABC123"),
                Vin.of("1HGCR2F83HA000000"),
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(sampleCustomer.id()),
                Optional.empty()
        );

        sampleAppointment = Appointment.schedule(
                AppointmentId.generate(),
                TenantId.of(tenantUuid),
                BranchId.generate(),
                sampleCustomer.id(),
                sampleVehicle.id(),
                Instant.now().plusSeconds(86400),
                60,
                "Alineamiento regular"
        );
    }

    @Nested
    @DisplayName("CustomersController Canonical Endpoints")
    class CustomersControllerTests {

        private CustomersController controller;

        @BeforeEach
        void setUp() {
            controller = new CustomersController(
                    customerCommandService,
                    customerQueryService,
                    vehicleQueryService
            );
        }

        @Test
        @DisplayName("2.1 [POST] /api/v1/customers/individuals -> 201 Created")
        void shouldRegisterIndividualCustomer() {
            CreateIndividualCustomerResource resource = new CreateIndividualCustomerResource(
                    "Juan", "Perez", "45871234", "juan@gmail.com", "+51987654321"
            );
            when(customerCommandService.handle(any(RegisterIndividualCustomerCommand.class)))
                    .thenReturn(Result.success(sampleCustomer));

            ResponseEntity<?> response = controller.registerIndividualCustomer(testUser, resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
        }

        @Test
        @DisplayName("2.2 [POST] /api/v1/customers/companies -> 201 Created")
        void shouldRegisterCompanyCustomer() {
            CreateCompanyCustomerResource resource = new CreateCompanyCustomerResource(
                    "Transportes Express SAC", "20100070970", "contacto@express.pe", "+51987654322"
            );
            Customer company = Customer.registerCompany(
                    CustomerId.generate(),
                    TenantId.of(tenantUuid),
                    "Transportes Express SAC",
                    TaxId.of("20100070970"),
                    EmailAddress.of("contacto@express.pe"),
                    PhoneNumber.of("+51987654322")
            );
            when(customerCommandService.handle(any(RegisterCompanyCustomerCommand.class)))
                    .thenReturn(Result.success(company));

            ResponseEntity<?> response = controller.registerCompanyCustomer(testUser, resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("2.3 [GET] /api/v1/customers -> 200 OK")
        void shouldGetCustomers() {
            when(customerQueryService.handle(any(GetCustomersByTenantIdQuery.class))).thenReturn(List.of(sampleCustomer));

            ResponseEntity<?> response = controller.getCustomers(testUser, null, null, null, 0, 20);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("2.4 [GET] /api/v1/customers/{id} -> 200 OK")
        void shouldGetCustomerById() {
            when(customerQueryService.handle(any(GetCustomerByIdQuery.class))).thenReturn(Optional.of(sampleCustomer));

            ResponseEntity<?> response = controller.getCustomerById(testUser, sampleCustomer.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("2.4 [GET] /api/v1/customers/{id} -> 404 Not Found")
        void shouldReturn404WhenCustomerNotFound() {
            when(customerQueryService.handle(any(GetCustomerByIdQuery.class))).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getCustomerById(testUser, UUID.randomUUID());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("2.5 [PUT] /api/v1/customers/{id}/contact -> 200 OK")
        void shouldUpdateCustomerContact() {
            UpdateCustomerContactResource resource = new UpdateCustomerContactResource(
                    "nuevo@email.com", "+51999888777"
            );
            when(customerCommandService.handle(any(UpdateCustomerContactCommand.class)))
                    .thenReturn(Result.success(sampleCustomer));

            ResponseEntity<?> response = controller.updateContact(testUser, sampleCustomer.id().value(), resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("2.6 [GET] /api/v1/customers/{id}/vehicles -> 200 OK")
        void shouldGetCustomerVehicles() {
            when(vehicleQueryService.handle(any(GetVehiclesByCustomerIdQuery.class))).thenReturn(List.of(sampleVehicle));

            ResponseEntity<?> response = controller.getCustomerVehicles(sampleCustomer.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("VehiclesController Canonical Endpoints")
    class VehiclesControllerTests {

        private VehiclesController controller;

        @BeforeEach
        void setUp() {
            controller = new VehiclesController(vehicleCommandService, vehicleQueryService);
        }

        @Test
        @DisplayName("3.1 [POST] /api/v1/vehicles -> 201 Created")
        void shouldRegisterVehicle() {
            CreateVehicleResource resource = new CreateVehicleResource(
                    "ABC123", "1HGCR2F83HA000000", "Toyota", "Corolla", 2022,
                    "GASOLINE", sampleCustomer.id().value(), null
            );
            when(vehicleCommandService.handle(any(RegisterVehicleCommand.class)))
                    .thenReturn(Result.success(sampleVehicle));

            ResponseEntity<?> response = controller.registerVehicle(testUser, resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("3.2 [GET] /api/v1/vehicles/{id} -> 200 OK")
        void shouldGetVehicleById() {
            when(vehicleQueryService.handle(any(GetVehicleByIdQuery.class))).thenReturn(Optional.of(sampleVehicle));

            ResponseEntity<?> response = controller.getVehicleById(sampleVehicle.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("3.2 [GET] /api/v1/vehicles/{id} -> 404 Not Found")
        void shouldReturn404WhenVehicleNotFound() {
            when(vehicleQueryService.handle(any(GetVehicleByIdQuery.class))).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getVehicleById(UUID.randomUUID());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("3.3 [GET] /api/v1/vehicles/by-plate/{plate} -> 200 OK")
        void shouldGetVehicleByPlate() {
            when(vehicleQueryService.handle(any(GetVehicleByPlateQuery.class))).thenReturn(Optional.of(sampleVehicle));

            ResponseEntity<?> response = controller.getVehicleByPlate("ABC123");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("3.4 [POST] /api/v1/vehicles/{id}/ownerships -> 201 Created")
        void shouldTransferOwnership() {
            TransferVehicleOwnershipResource resource = new TransferVehicleOwnershipResource(
                    UUID.randomUUID(), LocalDate.now()
            );
            when(vehicleCommandService.handle(any(TransferVehicleOwnershipCommand.class)))
                    .thenReturn(Result.success(sampleVehicle));

            ResponseEntity<?> response = controller.transferOwnership(testUser, sampleVehicle.id().value(), resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("3.5 [GET] /api/v1/vehicles/{id}/ownerships -> 200 OK")
        void shouldGetOwnershipHistory() {
            VehicleOwnership ownership = VehicleOwnership.create(
                    VehicleOwnershipId.generate(),
                    sampleVehicle.id(),
                    sampleCustomer.id(),
                    null,
                    LocalDate.now().minusMonths(6)
            );
            when(vehicleQueryService.handle(any(GetVehicleOwnershipHistoryQuery.class))).thenReturn(List.of(ownership));

            ResponseEntity<?> response = controller.getOwnershipHistory(sampleVehicle.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("3.6 [GET] /api/v1/vehicles/my-vehicles -> 200 OK")
        void shouldGetMyVehicles() {
            when(vehicleQueryService.handle(any(GetVehiclesByUserIdQuery.class))).thenReturn(List.of(sampleVehicle));

            ResponseEntity<?> response = controller.getMyVehicles(testUser);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("AppointmentsController Canonical Endpoints")
    class AppointmentsControllerTests {

        private AppointmentsController controller;

        @BeforeEach
        void setUp() {
            controller = new AppointmentsController(appointmentCommandService, appointmentQueryService);
        }

        @Test
        @DisplayName("4.1 [POST] /api/v1/appointments -> 201 Created")
        void shouldScheduleAppointment() {
            ScheduleAppointmentResource resource = new ScheduleAppointmentResource(
                    UUID.randomUUID(), sampleCustomer.id().value(), sampleVehicle.id().value(),
                    Instant.now().plusSeconds(86400), 60, "Mantenimiento 10k"
            );
            when(appointmentCommandService.handle(any(ScheduleAppointmentCommand.class)))
                    .thenReturn(Result.success(sampleAppointment));

            ResponseEntity<?> response = controller.scheduleAppointment(testUser, resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("4.2 [GET] /api/v1/appointments -> 200 OK")
        void shouldGetAppointments() {
            when(appointmentQueryService.handle(any(GetAppointmentsByTenantAndBranchQuery.class))).thenReturn(List.of(sampleAppointment));

            ResponseEntity<?> response = controller.getAppointments(testUser, UUID.randomUUID(), LocalDate.now(), null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("4.3 [GET] /api/v1/appointments/{id} -> 200 OK")
        void shouldGetAppointmentById() {
            when(appointmentQueryService.handle(any(GetAppointmentByIdQuery.class))).thenReturn(Optional.of(sampleAppointment));

            ResponseEntity<?> response = controller.getAppointmentById(testUser, sampleAppointment.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("4.3 [GET] /api/v1/appointments/{id} -> 404 Not Found")
        void shouldReturn404WhenAppointmentNotFound() {
            when(appointmentQueryService.handle(any(GetAppointmentByIdQuery.class))).thenReturn(Optional.empty());

            ResponseEntity<?> response = controller.getAppointmentById(testUser, UUID.randomUUID());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("4.4 [POST] /api/v1/appointments/{id}/confirm -> 200 OK")
        void shouldConfirmAppointment() {
            when(appointmentCommandService.handle(any(ConfirmAppointmentCommand.class)))
                    .thenReturn(Result.success(sampleAppointment));

            ResponseEntity<?> response = controller.confirmAppointment(testUser, sampleAppointment.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("4.5 [POST] /api/v1/appointments/{id}/arrive -> 200 OK")
        void shouldMarkAppointmentArrived() {
            when(appointmentCommandService.handle(any(MarkAppointmentArrivedCommand.class)))
                    .thenReturn(Result.success(sampleAppointment));

            ResponseEntity<?> response = controller.markArrived(testUser, sampleAppointment.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("4.6 [POST] /api/v1/appointments/{id}/reschedule -> 200 OK")
        void shouldRescheduleAppointment() {
            RescheduleAppointmentResource resource = new RescheduleAppointmentResource(Instant.now().plusSeconds(172800));
            when(appointmentCommandService.handle(any(RescheduleAppointmentCommand.class)))
                    .thenReturn(Result.success(sampleAppointment));

            ResponseEntity<?> response = controller.rescheduleAppointment(testUser, sampleAppointment.id().value(), resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("4.7 [POST] /api/v1/appointments/{id}/cancel -> 200 OK")
        void shouldCancelAppointment() {
            CancelAppointmentResource resource = new CancelAppointmentResource("Motivo de cancelación");
            when(appointmentCommandService.handle(any(CancelAppointmentCommand.class)))
                    .thenReturn(Result.success(sampleAppointment));

            ResponseEntity<?> response = controller.cancelAppointment(testUser, sampleAppointment.id().value(), resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("CustomerMembershipsController Canonical Endpoints")
    class CustomerMembershipsControllerTests {

        private CustomerMembershipsController controller;

        @BeforeEach
        void setUp() {
            controller = new CustomerMembershipsController(
                    customerMembershipCommandService,
                    customerMembershipQueryService
            );
        }

        @Test
        @DisplayName("5.1 [POST] /api/v1/customers/{id}/memberships -> 201 Created")
        void shouldInviteCompanyMember() {
            InviteCustomerMemberResource resource = new InviteCustomerMemberResource(
                    UUID.randomUUID(), "FLEET_ADMIN"
            );
            CustomerMembership membership = CustomerMembership.create(
                    CustomerMembershipId.generate(),
                    sampleCustomer.id(),
                    com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId.generate(),
                    FleetRole.FLEET_ADMIN
            );
            when(customerMembershipCommandService.handle(any(InviteCustomerMemberCommand.class)))
                    .thenReturn(Result.success(membership));

            ResponseEntity<?> response = controller.inviteMember(testUser, sampleCustomer.id().value(), resource);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }

        @Test
        @DisplayName("5.2 [GET] /api/v1/customers/{id}/memberships -> 200 OK")
        void shouldGetCompanyMembers() {
            CustomerMembership membership = CustomerMembership.create(
                    CustomerMembershipId.generate(),
                    sampleCustomer.id(),
                    com.andeva.atelier.platform.shared.domain.model.valueobjects.UserId.generate(),
                    FleetRole.FLEET_OPERATOR
            );
            when(customerMembershipQueryService.handle(any(GetCustomerMembersByCustomerIdQuery.class))).thenReturn(List.of(membership));

            ResponseEntity<?> response = controller.getMembersByCustomerId(sampleCustomer.id().value());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("5.3 [DELETE] /api/v1/customers/{id}/memberships/{userId} -> 204 No Content")
        void shouldRevokeCompanyMember() {
            when(customerMembershipCommandService.handle(any(RevokeCustomerMemberCommand.class)))
                    .thenReturn(Result.success(null));

            ResponseEntity<?> response = controller.revokeMember(testUser, sampleCustomer.id().value(), UUID.randomUUID());

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        }
    }
}
