package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.TelemetryIngestionCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.TelemetryLogQueryService;
import com.andeva.atelier.platform.iot.domain.model.aggregates.TelemetryRecord;
import com.andeva.atelier.platform.iot.domain.model.commands.IngestTelemetryBatchCommand;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.BatteryVoltage;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineRpm;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.EngineTemperature;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.FuelLevel;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.GeoCoordinates;
import com.andeva.atelier.platform.iot.domain.model.valueobjects.VehicleSpeed;
import com.andeva.atelier.platform.iot.interfaces.rest.advice.IoTExceptionHandler;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.TelemetryBatchRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.TelemetryReadingItemDto;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.TelemetryResourceAssembler;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link TelemetryIngestionController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryIngestionController Unit Tests")
class TelemetryIngestionControllerTest {

    @Mock
    private TelemetryIngestionCommandService telemetryCommandService;

    @Mock
    private TelemetryLogQueryService telemetryQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final UUID vehicleUuid = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TelemetryResourceAssembler assembler = new TelemetryResourceAssembler();
        TelemetryIngestionController controller = new TelemetryIngestionController(
                telemetryCommandService,
                telemetryQueryService,
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
                                "mechanic@andeva.pe",
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
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Should ingest telemetry batch successfully with 202 Accepted")
    void shouldIngestTelemetryBatchSuccessfully() throws Exception {
        TelemetryReadingItemDto reading = new TelemetryReadingItemDto(
                Instant.now(),
                -12.046374,
                -77.042793,
                65,
                92.0,
                2400,
                75.5,
                13.8
        );
        TelemetryBatchRequest request = new TelemetryBatchRequest(vehicleUuid, List.of(reading));

        mockMvc.perform(post("/api/v1/iot/telemetry/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.ingestedCount").value(1))
                .andExpect(jsonPath("$.anomalyDetected").value(false));

        verify(telemetryCommandService).handle(any(IngestTelemetryBatchCommand.class));
    }

    @Test
    @DisplayName("Should reject telemetry batch when validation fails")
    void shouldRejectTelemetryBatchWhenValidationFails() throws Exception {
        TelemetryBatchRequest invalidRequest = new TelemetryBatchRequest(null, Collections.emptyList());

        mockMvc.perform(post("/api/v1/iot/telemetry/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"));
    }

    @Test
    @DisplayName("Should return latest telemetry reading successfully")
    void shouldReturnLatestTelemetrySuccessfully() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        TenantId tenantId = TenantId.of(tenantUuid);
        TelemetryRecord record = TelemetryRecord.of(
                Instant.now(),
                vehicleId,
                tenantId,
                GeoCoordinates.of(-12.046374, -77.042793),
                VehicleSpeed.of(60.0),
                EngineTemperature.of(90.0),
                EngineRpm.of(2200),
                FuelLevel.of(65.0),
                BatteryVoltage.of(14.1)
        );

        when(telemetryQueryService.getLatestTelemetry(vehicleId)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/v1/iot/telemetry/vehicle/{vehicleId}/latest", vehicleUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.speedKmh").value(60))
                .andExpect(jsonPath("$.engineTempCelsius").value(90.0))
                .andExpect(jsonPath("$.engineRpm").value(2200))
                .andExpect(jsonPath("$.fuelPercentage").value(65.0))
                .andExpect(jsonPath("$.batteryVoltage").value(14.1));
    }

    @Test
    @DisplayName("Should return 404 when vehicle has no telemetry records")
    void shouldReturnNotFoundWhenNoTelemetryRecordsFound() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        when(telemetryQueryService.getLatestTelemetry(vehicleId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/iot/telemetry/vehicle/{vehicleId}/latest", vehicleUuid))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ERR_TELEMETRY_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should return 403 when vehicle latest telemetry belongs to another tenant")
    void shouldReturnForbiddenWhenLatestTelemetryBelongsToAnotherTenant() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        TenantId otherTenant = TenantId.of(UUID.randomUUID());
        TelemetryRecord record = TelemetryRecord.of(
                Instant.now(),
                vehicleId,
                otherTenant,
                GeoCoordinates.of(-12.046374, -77.042793),
                VehicleSpeed.of(60.0),
                EngineTemperature.of(90.0),
                EngineRpm.of(2200),
                FuelLevel.of(65.0),
                BatteryVoltage.of(14.1)
        );

        when(telemetryQueryService.getLatestTelemetry(vehicleId)).thenReturn(Optional.of(record));

        mockMvc.perform(get("/api/v1/iot/telemetry/vehicle/{vehicleId}/latest", vehicleUuid))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }

    @Test
    @DisplayName("Should return aggregated telemetry history")
    void shouldReturnAggregatedTelemetryHistory() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        TenantId tenantId = TenantId.of(tenantUuid);
        Instant now = Instant.now();
        TelemetryRecord record = TelemetryRecord.of(
                now,
                vehicleId,
                tenantId,
                GeoCoordinates.of(-12.046374, -77.042793),
                VehicleSpeed.of(50.0),
                EngineTemperature.of(88.0),
                EngineRpm.of(2000),
                FuelLevel.of(70.0),
                BatteryVoltage.of(13.9)
        );

        when(telemetryQueryService.getAggregatedTelemetry(eq(vehicleId), any(Instant.class), any(Instant.class), eq("1 hour")))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/iot/telemetry/vehicle/{vehicleId}/history", vehicleUuid)
                        .param("bucket", "1 hour"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$[0].avgSpeedKmh").value(50))
                .andExpect(jsonPath("$[0].sampleCount").value(1));
    }

    @Test
    @DisplayName("Should reject history query when start date is after end date")
    void shouldRejectHistoryWhenStartIsAfterEnd() throws Exception {
        Instant now = Instant.now();
        Instant future = now.plus(2, ChronoUnit.DAYS);

        mockMvc.perform(get("/api/v1/iot/telemetry/vehicle/{vehicleId}/history", vehicleUuid)
                        .param("from", future.toString())
                        .param("to", now.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Start date cannot be after end date"));
    }

    @Test
    @DisplayName("Should return 403 when vehicle telemetry history belongs to another tenant")
    void shouldReturnForbiddenWhenTelemetryHistoryBelongsToAnotherTenant() throws Exception {
        VehicleId vehicleId = VehicleId.of(vehicleUuid);
        TenantId otherTenant = TenantId.of(UUID.randomUUID());
        TelemetryRecord record = TelemetryRecord.of(
                Instant.now(),
                vehicleId,
                otherTenant,
                GeoCoordinates.of(-12.046374, -77.042793),
                VehicleSpeed.of(50.0),
                EngineTemperature.of(88.0),
                EngineRpm.of(2000),
                FuelLevel.of(70.0),
                BatteryVoltage.of(13.9)
        );

        when(telemetryQueryService.getAggregatedTelemetry(eq(vehicleId), any(Instant.class), any(Instant.class), eq("1 hour")))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/v1/iot/telemetry/vehicle/{vehicleId}/history", vehicleUuid)
                        .param("bucket", "1 hour"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Access Denied"));
    }
}
