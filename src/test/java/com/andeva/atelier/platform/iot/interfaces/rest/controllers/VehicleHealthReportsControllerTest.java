package com.andeva.atelier.platform.iot.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.iot.application.commandservices.VehicleHealthReportCommandService;
import com.andeva.atelier.platform.iot.application.queryservices.VehicleHealthReportQueryService;
import com.andeva.atelier.platform.iot.domain.model.commands.GenerateVehicleHealthReportCommand;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.DtcTelemetryCorrelationDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.PredictiveRiskDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.RecommendedServiceActionDto;
import com.andeva.atelier.platform.iot.domain.model.dto.ai.VehicleHealthReportAiDto;
import com.andeva.atelier.platform.iot.domain.model.queries.ExportVehicleHealthReportPdfQuery;
import com.andeva.atelier.platform.iot.domain.model.queries.GetLatestVehicleHealthReportQuery;
import com.andeva.atelier.platform.iot.interfaces.rest.advice.IoTExceptionHandler;
import com.andeva.atelier.platform.iot.interfaces.rest.resources.requests.GenerateHealthReportRequest;
import com.andeva.atelier.platform.iot.interfaces.rest.transform.VehicleHealthReportResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.VehicleId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link VehicleHealthReportsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleHealthReportsController Unit Tests")
class VehicleHealthReportsControllerTest {

    @Mock
    private VehicleHealthReportCommandService reportCommandService;

    @Mock
    private VehicleHealthReportQueryService reportQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final UUID vehicleUuid = UUID.randomUUID();
    private VehicleHealthReportAiDto reportDto;

    @BeforeEach
    void setUp() {
        VehicleHealthReportResourceAssembler assembler = new VehicleHealthReportResourceAssembler();
        VehicleHealthReportsController controller = new VehicleHealthReportsController(
                reportCommandService,
                reportQueryService,
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
                                "headmechanic@andeva.pe",
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

        reportDto = new VehicleHealthReportAiDto(
                85,
                "FAIR",
                "Cooling system shows minor temperature spikes under load.",
                List.of(new com.andeva.atelier.platform.iot.domain.model.dto.ai.SubsystemEvaluationDto("Cooling", "FAIR", 85, "Minor spikes")),
                List.of(new PredictiveRiskDto("COOLING_SYSTEM", BigDecimal.valueOf(0.75), 14, "Overheating risk")),
                List.of(new RecommendedServiceActionDto("SRV-COOL-01", "Radiator Flush", "HIGH", BigDecimal.valueOf(180.00), "Prevent overheating")),
                List.of(new DtcTelemetryCorrelationDto("P0117", "102C", "ECT sensor short circuit"))
        );
    }

    @Test
    @DisplayName("Should generate health report synchronously with 201 Created")
    void shouldGenerateHealthReportSynchronously() throws Exception {
        when(reportCommandService.handle(any(GenerateVehicleHealthReportCommand.class))).thenReturn(reportDto);

        GenerateHealthReportRequest request = new GenerateHealthReportRequest(30, true);

        mockMvc.perform(post("/api/v1/iot/health-reports/generate")
                        .param("vehicleId", vehicleUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.overallHealthScore").value(85))
                .andExpect(jsonPath("$.executiveSummary").value("Cooling system shows minor temperature spikes under load."))
                .andExpect(jsonPath("$.totalRisksDetected").value(1));

        verify(reportCommandService).handle(any(GenerateVehicleHealthReportCommand.class));
    }

    @Test
    @DisplayName("Should queue asynchronous health report generation with 202 Accepted")
    void shouldQueueAsyncHealthReportGeneration() throws Exception {
        GenerateHealthReportRequest request = new GenerateHealthReportRequest(60, false);

        mockMvc.perform(post("/api/v1/iot/health-reports/generate-async")
                        .param("vehicleId", vehicleUuid.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").isNotEmpty())
                .andExpect(jsonPath("$.status").value("QUEUED"))
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()));
    }

    @Test
    @DisplayName("Should get latest vehicle health report")
    void shouldGetLatestHealthReport() throws Exception {
        when(reportQueryService.handle(any(GetLatestVehicleHealthReportQuery.class)))
                .thenReturn(Optional.of(reportDto));

        mockMvc.perform(get("/api/v1/iot/health-reports/latest")
                        .param("vehicleId", vehicleUuid.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleId").value(vehicleUuid.toString()))
                .andExpect(jsonPath("$.overallHealthScore").value(85))
                .andExpect(jsonPath("$.totalRisksDetected").value(1));
    }

    @Test
    @DisplayName("Should return 404 when latest report is not found")
    void shouldReturnNotFoundWhenLatestReportNotFound() throws Exception {
        when(reportQueryService.handle(any(GetLatestVehicleHealthReportQuery.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/iot/health-reports/latest")
                        .param("vehicleId", vehicleUuid.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ERR_HEALTH_REPORT_NOT_FOUND"));
    }

    @Test
    @DisplayName("Should download PDF health report successfully")
    void shouldDownloadPdfReportSuccessfully() throws Exception {
        UUID reportId = UUID.randomUUID();
        byte[] fakePdfBytes = "%PDF-1.4 Mock PDF Content".getBytes();

        when(reportQueryService.handle(any(ExportVehicleHealthReportPdfQuery.class)))
                .thenReturn(fakePdfBytes);

        mockMvc.perform(get("/api/v1/iot/health-reports/{reportId}/pdf", reportId)
                        .param("vehicleId", vehicleUuid.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"vehicle-health-report-" + reportId + ".pdf\""))
                .andExpect(content().bytes(fakePdfBytes));

        verify(reportQueryService).handle(any(ExportVehicleHealthReportPdfQuery.class));
    }
}
