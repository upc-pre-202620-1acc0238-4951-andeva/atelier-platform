package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.commandservices.SeriesConfigurationCommandService;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.SeriesConfiguration;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.SeriesConfigurationId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;
import com.andeva.atelier.platform.invoicing.application.queryservices.SeriesConfigurationQueryService;
import com.andeva.atelier.platform.invoicing.interfaces.rest.advice.InvoicingExceptionHandler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.SeriesConfigurationResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.ConfigureSeriesRequest;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.UpdateSeriesStatusRequest;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test suite for {@link SeriesConfigurationController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeriesConfigurationController Unit Tests")
class SeriesConfigurationControllerTest {

    @Mock
    private SeriesConfigurationCommandService seriesCommandService;

    @Mock
    private SeriesConfigurationQueryService seriesQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TenantId tenantId;
    private BranchId branchId;
    private SeriesConfiguration sampleSeries;

    @BeforeEach
    void setUp() {
        SeriesConfigurationResourceAssembler assembler = new SeriesConfigurationResourceAssembler();
        SeriesConfigurationController controller = new SeriesConfigurationController(
                seriesCommandService,
                seriesQueryService,
                assembler
        );

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");

        HandlerMethodArgumentResolver userDetailsResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                String headerTenantId = webRequest.getHeader("X-Tenant-Id");
                if (headerTenantId == null) {
                    return null;
                }
                return new CustomUserDetails(
                        UUID.randomUUID(),
                        "owner@andeva.pe",
                        "hash",
                        UUID.fromString(headerTenantId),
                        List.of(new SimpleGrantedAuthority("ROLE_WORKSHOP_OWNER")),
                        true
                );
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(userDetailsResolver)
                .setControllerAdvice(new InvoicingExceptionHandler(messageSource))
                .build();

        objectMapper = new ObjectMapper();
        tenantId = TenantId.generate();
        branchId = BranchId.generate();

        sampleSeries = SeriesConfiguration.create(
                tenantId,
                branchId,
                VoucherType.FACTURA,
                new VoucherSerie("F001"),
                0
        );
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/series-configurations configures new series and returns 201 Created")
    void configureSeriesSuccess() throws Exception {
        when(seriesCommandService.handle(any())).thenReturn(sampleSeries);

        ConfigureSeriesRequest request = new ConfigureSeriesRequest(
                branchId.value(),
                "01",
                "F001",
                0
        );

        mockMvc.perform(post("/api/v1/invoicing/series-configurations")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(sampleSeries.getId().value().toString()))
                .andExpect(jsonPath("$.serie").value("F001"))
                .andExpect(jsonPath("$.currentCorrelative").value(0))
                .andExpect(jsonPath("$.active").value(true));

        verify(seriesCommandService).handle(any());
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/series-configurations returns 400 Bad Request on invalid series length")
    void configureSeriesInvalidLength() throws Exception {
        ConfigureSeriesRequest request = new ConfigureSeriesRequest(
                branchId.value(),
                "01",
                "F01",
                0
        );

        mockMvc.perform(post("/api/v1/invoicing/series-configurations")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/series-configurations returns 403 Forbidden without tenant context")
    void configureSeriesMissingTenant() throws Exception {
        ConfigureSeriesRequest request = new ConfigureSeriesRequest(
                branchId.value(),
                "01",
                "F001",
                0
        );

        mockMvc.perform(post("/api/v1/invoicing/series-configurations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/series-configurations/branch/{branchId} returns 200 OK")
    void getSeriesByBranchSuccess() throws Exception {
        when(seriesQueryService.getSeriesByBranch(tenantId, branchId))
                .thenReturn(List.of(sampleSeries));

        mockMvc.perform(get("/api/v1/invoicing/series-configurations/branch/{branchId}", branchId.value())
                        .header("X-Tenant-Id", tenantId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].serie").value("F001"))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/invoicing/series-configurations/{id}/status activates series and returns 200 OK")
    void activateSeriesSuccess() throws Exception {
        UpdateSeriesStatusRequest request = new UpdateSeriesStatusRequest(true);

        mockMvc.perform(put("/api/v1/invoicing/series-configurations/{id}/status", sampleSeries.getId().value())
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(seriesCommandService).activateSeries(SeriesConfigurationId.of(sampleSeries.getId().value()), tenantId);
    }

    @Test
    @DisplayName("PUT /api/v1/invoicing/series-configurations/{id}/status deactivates series and returns 200 OK")
    void deactivateSeriesSuccess() throws Exception {
        UpdateSeriesStatusRequest request = new UpdateSeriesStatusRequest(false);

        mockMvc.perform(put("/api/v1/invoicing/series-configurations/{id}/status", sampleSeries.getId().value())
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(seriesCommandService).deactivateSeries(SeriesConfigurationId.of(sampleSeries.getId().value()), tenantId);
    }
}
