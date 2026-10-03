package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.AppointmentQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Appointment;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Customer;
import com.andeva.atelier.platform.crm.domain.model.aggregates.Vehicle;
import com.andeva.atelier.platform.crm.domain.model.enums.AppointmentStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerStatus;
import com.andeva.atelier.platform.crm.domain.model.enums.CustomerType;
import com.andeva.atelier.platform.crm.domain.model.enums.EngineType;
import com.andeva.atelier.platform.crm.domain.model.ids.AppointmentId;
import com.andeva.atelier.platform.crm.domain.model.queries.GetAppointmentByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomerByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetCustomersByTenantIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByIdQuery;
import com.andeva.atelier.platform.crm.domain.model.queries.GetVehicleByPlateQuery;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.LicensePlate;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.PersonName;
import com.andeva.atelier.platform.crm.domain.model.valueobjects.Vin;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateCompanyCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateIndividualCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateVehicleResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.ScheduleAppointmentResource;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.application.result.Result;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.PhoneNumber;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QA Web REST Controller test suite validating HTTP endpoints, status codes, and JSON serialization.
 *
 * @author Adiel Sanchez Santin
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("QA REST Controller Endpoint Simulation Tests for CRM")
class CrmRestControllerIntegrationTest {

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

    private MockMvc customerMvc;
    private MockMvc vehicleMvc;
    private MockMvc appointmentMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID branchUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver authResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(CustomUserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return new CustomUserDetails(
                        userUuid,
                        "adiel.sanchez3110@gmail.com",
                        "hashedPassword",
                        tenantUuid,
                        Collections.emptyList(),
                        true
                );
            }
        };

        CustomersController customersController = new CustomersController(customerCommandService, customerQueryService);
        customerMvc = MockMvcBuilders.standaloneSetup(customersController)
                .setCustomArgumentResolvers(authResolver)
                .build();

        VehiclesController vehiclesController = new VehiclesController(vehicleCommandService, vehicleQueryService);
        vehicleMvc = MockMvcBuilders.standaloneSetup(vehiclesController)
                .setCustomArgumentResolvers(authResolver)
                .build();

        AppointmentsController appointmentsController = new AppointmentsController(appointmentCommandService, appointmentQueryService);
        appointmentMvc = MockMvcBuilders.standaloneSetup(appointmentsController)
                .setCustomArgumentResolvers(authResolver)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("QA-REST-001: POST /api/v1/crm/customers/individual creates natural person customer with 201 Created")
    void testRegisterIndividualCustomerEndpoint() throws Exception {
        UUID customerUuid = UUID.randomUUID();
        Customer customer = Customer.registerIndividual(
                CustomerId.of(customerUuid),
                TenantId.of(tenantUuid),
                PersonName.of("Carlos", "Mendoza"),
                TaxId.of("47891234"),
                EmailAddress.of("carlos.mendoza@example.com"),
                PhoneNumber.of("+51987654321")
        );

        when(customerCommandService.handle(any(com.andeva.atelier.platform.crm.domain.model.commands.RegisterIndividualCustomerCommand.class)))
                .thenReturn(Result.success(customer));

        CreateIndividualCustomerResource resource = new CreateIndividualCustomerResource(
                "Carlos",
                "Mendoza",
                "47891234",
                "carlos.mendoza@example.com",
                "+51987654321"
        );

        customerMvc.perform(post("/api/v1/crm/customers/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(customerUuid.toString()))
                .andExpect(jsonPath("$.type").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.firstName").value("Carlos"))
                .andExpect(jsonPath("$.lastName").value("Mendoza"))
                .andExpect(jsonPath("$.taxId").value("47891234"));
    }

    @Test
    @DisplayName("QA-REST-002: POST /api/v1/crm/customers/company creates corporate customer with 201 Created")
    void testRegisterCompanyCustomerEndpoint() throws Exception {
        UUID customerUuid = UUID.randomUUID();
        Customer company = Customer.registerCompany(
                CustomerId.of(customerUuid),
                TenantId.of(tenantUuid),
                "Transportes del Sur S.A.C.",
                TaxId.of("20131312955"),
                EmailAddress.of("ops@transur.pe"),
                PhoneNumber.of("+5114567890")
        );

        when(customerCommandService.handle(any(com.andeva.atelier.platform.crm.domain.model.commands.RegisterCompanyCustomerCommand.class)))
                .thenReturn(Result.success(company));

        CreateCompanyCustomerResource resource = new CreateCompanyCustomerResource(
                "Transportes del Sur S.A.C.",
                "20131312955",
                "ops@transur.pe",
                "+5114567890"
        );

        customerMvc.perform(post("/api/v1/crm/customers/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(customerUuid.toString()))
                .andExpect(jsonPath("$.type").value("COMPANY"))
                .andExpect(jsonPath("$.companyName").value("Transportes del Sur S.A.C."))
                .andExpect(jsonPath("$.taxId").value("20131312955"));
    }

    @Test
    @DisplayName("QA-REST-003: GET /api/v1/crm/customers lists customer portfolio with 200 OK")
    void testGetCustomersListEndpoint() throws Exception {
        UUID customerUuid = UUID.randomUUID();
        Customer customer = Customer.registerIndividual(
                CustomerId.of(customerUuid),
                TenantId.of(tenantUuid),
                PersonName.of("Carlos", "Mendoza"),
                TaxId.of("47891234"),
                EmailAddress.of("carlos.mendoza@example.com"),
                PhoneNumber.of("+51987654321")
        );

        when(customerQueryService.handle(any(GetCustomersByTenantIdQuery.class)))
                .thenReturn(List.of(customer));

        customerMvc.perform(get("/api/v1/crm/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(customerUuid.toString()))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @DisplayName("QA-REST-004: POST /api/v1/crm/vehicles registers vehicle asset with 201 Created")
    void testRegisterVehicleEndpoint() throws Exception {
        UUID vehicleUuid = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();
        Vehicle vehicle = Vehicle.register(
                VehicleId.of(vehicleUuid),
                LicensePlate.of("ABC123"),
                Vin.of("1HGCR2F83HA123456"),
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(CustomerId.of(ownerUuid)),
                Optional.empty()
        );

        when(vehicleCommandService.handle(any(com.andeva.atelier.platform.crm.domain.model.commands.RegisterVehicleCommand.class)))
                .thenReturn(Result.success(vehicle));

        CreateVehicleResource resource = new CreateVehicleResource(
                "ABC123",
                "1HGCR2F83HA123456",
                "Toyota",
                "Corolla",
                2022,
                "gasoline",
                ownerUuid,
                null
        );

        vehicleMvc.perform(post("/api/v1/crm/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.plate").value("ABC123"))
                .andExpect(jsonPath("$.brand").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Corolla"));
    }

    @Test
    @DisplayName("QA-REST-005: GET /api/v1/crm/vehicles/by-plate/{plate} retrieves vehicle by plate with 200 OK")
    void testGetVehicleByPlateEndpoint() throws Exception {
        UUID vehicleUuid = UUID.randomUUID();
        UUID ownerUuid = UUID.randomUUID();
        Vehicle vehicle = Vehicle.register(
                VehicleId.of(vehicleUuid),
                LicensePlate.of("ABC123"),
                Vin.of("1HGCR2F83HA123456"),
                "Toyota",
                "Corolla",
                2022,
                EngineType.GASOLINE,
                Optional.of(CustomerId.of(ownerUuid)),
                Optional.empty()
        );

        when(vehicleQueryService.handle(any(GetVehicleByPlateQuery.class)))
                .thenReturn(Optional.of(vehicle));

        vehicleMvc.perform(get("/api/v1/crm/vehicles/by-plate/ABC-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.plate").value("ABC123"));
    }

    @Test
    @DisplayName("QA-REST-006: POST /api/v1/crm/appointments schedules appointment with 201 Created")
    void testScheduleAppointmentEndpoint() throws Exception {
        UUID appointmentUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();
        UUID vehicleUuid = UUID.randomUUID();
        Instant scheduledTime = Instant.now().plus(24, ChronoUnit.HOURS);

        Appointment appointment = Appointment.schedule(
                AppointmentId.of(appointmentUuid),
                TenantId.of(tenantUuid),
                BranchId.of(branchUuid),
                CustomerId.of(customerUuid),
                VehicleId.of(vehicleUuid),
                scheduledTime,
                60,
                "Mantenimiento preventivo"
        );

        when(appointmentCommandService.handle(any(com.andeva.atelier.platform.crm.domain.model.commands.ScheduleAppointmentCommand.class)))
                .thenReturn(Result.success(appointment));

        ScheduleAppointmentResource resource = new ScheduleAppointmentResource(
                branchUuid,
                customerUuid,
                vehicleUuid,
                scheduledTime,
                60,
                "Mantenimiento preventivo"
        );

        appointmentMvc.perform(post("/api/v1/crm/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resource)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(appointmentUuid.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.reason").value("Mantenimiento preventivo"));
    }

    @Test
    @DisplayName("QA-REST-007: POST /api/v1/crm/appointments/{id}/arrive transitions appointment to ARRIVED with 200 OK")
    void testMarkAppointmentArrivedEndpoint() throws Exception {
        UUID appointmentUuid = UUID.randomUUID();
        UUID customerUuid = UUID.randomUUID();
        UUID vehicleUuid = UUID.randomUUID();
        Instant scheduledTime = Instant.now().plus(24, ChronoUnit.HOURS);

        Appointment appointment = Appointment.schedule(
                AppointmentId.of(appointmentUuid),
                TenantId.of(tenantUuid),
                BranchId.of(branchUuid),
                CustomerId.of(customerUuid),
                VehicleId.of(vehicleUuid),
                scheduledTime,
                60,
                "Mantenimiento preventivo"
        );
        appointment.confirm();
        appointment.markArrived();

        when(appointmentCommandService.handle(any(com.andeva.atelier.platform.crm.domain.model.commands.MarkAppointmentArrivedCommand.class)))
                .thenReturn(Result.success(appointment));

        appointmentMvc.perform(post("/api/v1/crm/appointments/" + appointmentUuid + "/arrive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentUuid.toString()))
                .andExpect(jsonPath("$.status").value("ARRIVED"));
    }
}
