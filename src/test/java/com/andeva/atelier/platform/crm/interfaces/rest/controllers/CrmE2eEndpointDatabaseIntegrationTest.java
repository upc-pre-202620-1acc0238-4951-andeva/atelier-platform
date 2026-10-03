package com.andeva.atelier.platform.crm.interfaces.rest.controllers;

import com.andeva.atelier.platform.crm.application.commandservices.AppointmentCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.CustomerCommandService;
import com.andeva.atelier.platform.crm.application.commandservices.VehicleCommandService;
import com.andeva.atelier.platform.crm.application.queryservices.AppointmentQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.CustomerQueryService;
import com.andeva.atelier.platform.crm.application.queryservices.VehicleQueryService;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateCompanyCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateIndividualCustomerResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.CreateVehicleResource;
import com.andeva.atelier.platform.crm.interfaces.rest.resources.requests.ScheduleAppointmentResource;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-End REST Controller test against real PostgreSQL database.
 * Simulates genuine HTTP requests through controllers down to database persistence.
 *
 * @author Adiel Sanchez Santin
 */
@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/atelier_db",
        "spring.datasource.driver-class-name=org.postgresql.Driver",
        "spring.datasource.username=postgres",
        "spring.datasource.password=postgres",
        "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect",
        "spring.jpa.hibernate.ddl-auto=update"
})
@DisplayName("E2E HTTP REST Endpoints Test with PostgreSQL Persistence")
class CrmE2eEndpointDatabaseIntegrationTest {

    @Autowired
    private CustomerCommandService customerCommandService;

    @Autowired
    private CustomerQueryService customerQueryService;

    @Autowired
    private VehicleCommandService vehicleCommandService;

    @Autowired
    private VehicleQueryService vehicleQueryService;

    @Autowired
    private AppointmentCommandService appointmentCommandService;

    @Autowired
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
    @Transactional
    @DisplayName("E2E Flow: HTTP POST and GET across Customers, Vehicles, and Appointments with PostgreSQL")
    void shouldExecuteCompleteHttpRestFlowWithDatabasePersistence() throws Exception {
        String randomDni = String.format("%08d", 10000000 + (int) (Math.random() * 89999999));
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6);

        // 1. HTTP POST /api/v1/crm/customers/individual
        CreateIndividualCustomerResource individualRequest = new CreateIndividualCustomerResource(
                "Juan",
                "Perez",
                randomDni,
                "juan." + randomSuffix + "@example.com",
                "+51999888777"
        );

        MvcResult createIndividualResult = customerMvc.perform(post("/api/v1/crm/customers/individual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(individualRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.type").value("INDIVIDUAL"))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andReturn();

        JsonNode individualJson = objectMapper.readTree(createIndividualResult.getResponse().getContentAsString());
        UUID individualId = UUID.fromString(individualJson.get("id").asText());

        // 2. HTTP GET /api/v1/crm/customers/{id}
        customerMvc.perform(get("/api/v1/crm/customers/" + individualId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(individualId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"));

        // 3. HTTP POST /api/v1/crm/customers/company (SUNAT RUC)
        CreateCompanyCustomerResource companyRequest = new CreateCompanyCustomerResource(
                "Logistica Andina S.A.C.",
                "20131312955",
                "contacto." + randomSuffix + "@logandina.pe",
                "+5112223344"
        );

        MvcResult createCompanyResult = customerMvc.perform(post("/api/v1/crm/customers/company")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(companyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.type").value("COMPANY"))
                .andExpect(jsonPath("$.companyName").value("Logistica Andina S.A.C."))
                .andReturn();

        JsonNode companyJson = objectMapper.readTree(createCompanyResult.getResponse().getContentAsString());
        UUID companyId = UUID.fromString(companyJson.get("id").asText());
        assertThat(companyId).isNotNull();

        // 4. HTTP POST /api/v1/crm/vehicles
        String uniquePlate = "N" + (10 + (int)(Math.random() * 89)) + (100 + (int)(Math.random() * 899));
        String uniqueVin = "1HGCR2F83HA" + String.valueOf(100000 + (int)(Math.random() * 899999));

        CreateVehicleResource vehicleRequest = new CreateVehicleResource(
                uniquePlate,
                uniqueVin,
                "Nissan",
                "Sentra",
                2023,
                "gasoline",
                individualId,
                null
        );

        MvcResult createVehicleResult = vehicleMvc.perform(post("/api/v1/crm/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.brand").value("Nissan"))
                .andExpect(jsonPath("$.model").value("Sentra"))
                .andReturn();

        JsonNode vehicleJson = objectMapper.readTree(createVehicleResult.getResponse().getContentAsString());
        UUID vehicleId = UUID.fromString(vehicleJson.get("id").asText());

        // 5. HTTP GET /api/v1/crm/vehicles/by-plate/{plate}
        vehicleMvc.perform(get("/api/v1/crm/vehicles/by-plate/" + uniquePlate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId.toString()))
                .andExpect(jsonPath("$.brand").value("Nissan"));

        // 6. HTTP POST /api/v1/crm/appointments
        Instant scheduledTime = Instant.now().plus(48, ChronoUnit.HOURS);
        ScheduleAppointmentResource appointmentRequest = new ScheduleAppointmentResource(
                branchUuid,
                individualId,
                vehicleId,
                scheduledTime,
                90,
                "Cambio de aceite y filtros de rutina"
        );

        MvcResult createAppointmentResult = appointmentMvc.perform(post("/api/v1/crm/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(appointmentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode appointmentJson = objectMapper.readTree(createAppointmentResult.getResponse().getContentAsString());
        UUID appointmentId = UUID.fromString(appointmentJson.get("id").asText());

        // 7. HTTP POST /api/v1/crm/appointments/{id}/arrive
        appointmentMvc.perform(post("/api/v1/crm/appointments/" + appointmentId + "/arrive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("ARRIVED"));
    }
}
