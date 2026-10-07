package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.PredictiveAlertCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.PredictiveAlertQueryService;
import com.andeva.atelier.platform.iot.domain.exceptions.PredictiveAlertNotFoundException;
import com.andeva.atelier.platform.iot.domain.model.aggregates.PredictiveAlert;
import com.andeva.atelier.platform.iot.domain.model.commands.AcknowledgePredictiveAlertCommand;
import com.andeva.atelier.platform.iot.domain.model.commands.ConvertAlertToAppointmentCommand;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertSeverity;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertStatus;
import com.andeva.atelier.platform.iot.domain.model.enums.AlertType;
import com.andeva.atelier.platform.iot.domain.model.ids.AlertId;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.ConfidenceScore;
import com.andeva.atelier.platform.iot.interfaces.rest.advice.IoTExceptionHandler;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.AcknowledgeAlertRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.PredictiveAlertResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link PredictiveAlertsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PredictiveAlertsController Unit Tests")
class PredictiveAlertsControllerTest {

    @Mock
    private PredictiveAlertCommandService alertCommandService;

    @Mock
    private PredictiveAlertQueryService alertQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final UUID vehicleUuid = UUID.randomUUID();
    private final UUID alertUuid = UUID.randomUUID();
    private AlertId alertId;
    private PredictiveAlert alert;

    @BeforeEach
    void setUp() {
        PredictiveAlertResourceAssembler assembler = new PredictiveAlertResourceAssembler();
        PredictiveAlertsController controller = new PredictiveAlertsController(
                alertCommandService,
                alertQueryService,
                assembler
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().equals(CustomUserDetails.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter,
                                                  ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest,
                                                  WebDataBinderFactory binderFactory) {
                        return new CustomUserDetails(
                                userUuid,
                                "advisor@andeva.pe",
                                "hashedPassword",
                                tenantUuid,
                                Collections.emptyList(),
                                true
                        );
                    }
                })
                .setControllerAdvice(new IoTExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        alertId = AlertId.of(alertUuid);
        alert = PredictiveAlert.create(
                VehicleId.of(vehicleUuid),
                TenantId.of(tenantUuid),
                AlertType.ENGINE_OVERHEATING_RISK,
                ConfidenceScore.of(BigDecimal.valueOf(95.0)),
                "Critical coolant temperature anomaly detected"
        );
    }

    @Test
    @DisplayName("Should return active alerts for tenant dashboard")
    void shouldReturnActiveAlertsForTenant() throws Exception {
        TenantId tenantId = TenantId.of(tenantUuid);
        when(alertQueryService.getActiveAlertsByTenant(tenantId, AlertStatus.DISPATCHED))
                .thenReturn(List.of(alert));

        mockMvc.perform(get("/api/v1/iot/alerts/tenant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$[0].alertType").value("ENGINE_OVERHEATING_RISK"))
                .andExpect(jsonPath("$[0].confidenceScore").value(95.0))
                .andExpect(jsonPath("$[0].status").value("DISPATCHED"));
    }

    @Test
    @DisplayName("Should return historical alerts for vehicle")
    void shouldReturnAlertsByVehicle() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        when(alertQueryService.getAlertsByVehicle(vehicleId)).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/v1/iot/alerts/vehicle/{vehicleId}", vehicleUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$[0].alertType").value("ENGINE_OVERHEATING_RISK"));
    }

    @Test
    @DisplayName("Should return 403 when vehicle alerts belong to different tenant")
    void shouldReturnForbiddenWhenVehicleAlertsBelongToDifferentTenant() throws Exception {
        UUID otherTenant = UUID.randomUUID();
        PredictiveAlert crossTenantAlert = PredictiveAlert.create(
                VehicleId.of(vehicleUuid),
                TenantId.of(otherTenant),
                AlertType.ENGINE_OVERHEATING_RISK,
                ConfidenceScore.of(BigDecimal.valueOf(95.0)),
                "Cross tenant alert"
        );

        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        when(alertQueryService.getAlertsByVehicle(vehicleId)).thenReturn(List.of(crossTenantAlert));

        mockMvc.perform(get("/api/v1/iot/alerts/vehicle/{vehicleId}", vehicleUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }

    @Test
    @DisplayName("Should acknowledge alert successfully")
    void shouldAcknowledgeAlertSuccessfully() throws Exception {
        PredictiveAlert ackAlert = PredictiveAlert.create(
                VehicleId.of(vehicleUuid),
                TenantId.of(tenantUuid),
                AlertType.ENGINE_OVERHEATING_RISK,
                ConfidenceScore.of(BigDecimal.valueOf(95.0)),
                "Critical coolant temperature anomaly detected"
        );
        ackAlert.acknowledge();

        when(alertQueryService.findById(alertId))
                .thenReturn(Optional.of(alert))
                .thenReturn(Optional.of(ackAlert));

        AcknowledgeAlertRequest request = new AcknowledgeAlertRequest("Reviewed by service advisor");

        mockMvc.perform(patch("/api/v1/iot/alerts/{id}/acknowledge", alertUuid)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACKNOWLEDGED"));

        verify(alertCommandService).handle(any(AcknowledgePredictiveAlertCommand.class));
    }

    @Test
    @DisplayName("Should return 403 when acknowledging alert belonging to different tenant")
    void shouldReturnForbiddenWhenAcknowledgingCrossTenantAlert() throws Exception {
        UUID otherTenant = UUID.randomUUID();
        PredictiveAlert crossTenantAlert = PredictiveAlert.create(
                VehicleId.of(vehicleUuid),
                TenantId.of(otherTenant),
                AlertType.ENGINE_OVERHEATING_RISK,
                ConfidenceScore.of(BigDecimal.valueOf(95.0)),
                "Cross tenant alert"
        );

        when(alertQueryService.findById(alertId)).thenReturn(Optional.of(crossTenantAlert));

        mockMvc.perform(patch("/api/v1/iot/alerts/{id}/acknowledge", alertUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }

    @Test
    @DisplayName("Should return 404 when acknowledging non-existent alert")
    void shouldReturnNotFoundWhenAlertDoesNotExist() throws Exception {
        when(alertQueryService.findById(alertId)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/v1/iot/alerts/{id}/acknowledge", alertUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Predictive Alert Not Found"));
    }

    @Test
    @DisplayName("Should convert predictive alert to CRM appointment successfully")
    void shouldConvertAlertToAppointmentSuccessfully() throws Exception {
        UUID appointmentId = UUID.randomUUID();
        when(alertQueryService.findById(alertId)).thenReturn(Optional.of(alert));
        when(alertCommandService.handle(any(ConvertAlertToAppointmentCommand.class))).thenReturn(appointmentId);

        mockMvc.perform(post("/api/v1/iot/alerts/{id}/convert-to-appointment", alertUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appointmentId").value(appointmentId.toString()))
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));

        verify(alertCommandService).handle(any(ConvertAlertToAppointmentCommand.class));
    }
}
