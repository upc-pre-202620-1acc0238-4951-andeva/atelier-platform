package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.queryservices.CashFlowQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetCashFlowSummaryQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowMovement;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.CashFlowSummary;
import com.andeva.atelier.platform.invoicing.interfaces.rest.advice.InvoicingExceptionHandler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.CashFlowResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test suite for {@link FinancialReportsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FinancialReportsController Unit Tests")
class FinancialReportsControllerTest {

    @Mock
    private CashFlowQueryService cashFlowQueryService;

    private MockMvc mockMvc;
    private TenantId tenantId;
    private CashFlowSummary sampleSummary;
    private CashFlowMovement sampleMovement;

    @BeforeEach
    void setUp() {
        CashFlowResourceAssembler assembler = new CashFlowResourceAssembler();
        FinancialReportsController controller = new FinancialReportsController(
                cashFlowQueryService,
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

        tenantId = TenantId.generate();

        sampleSummary = new CashFlowSummary(
                Money.soles(5000.00),
                Money.soles(1500.00),
                Money.soles(500.00),
                Money.soles(3000.00)
        );

        sampleMovement = new CashFlowMovement(
                UUID.randomUUID(),
                Instant.now(),
                "INCOME",
                "BILLING",
                "Cobro de Factura",
                "F001-00000001",
                new BigDecimal("500.00"),
                new BigDecimal("3000.00")
        );
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/financial-reports/cash-flow returns 200 OK with summary and movements")
    void getCashFlowReportSuccess() throws Exception {
        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();

        when(cashFlowQueryService.handle(any(GetCashFlowSummaryQuery.class)))
                .thenReturn(sampleSummary);
        when(cashFlowQueryService.getCashFlowMovements(eq(tenantId), any(), any()))
                .thenReturn(List.of(sampleMovement));

        mockMvc.perform(get("/api/v1/invoicing/financial-reports/cash-flow")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses").value(2000.00)) // 1500 + 500
                .andExpect(jsonPath("$.netCashFlow").value(3000.00))
                .andExpect(jsonPath("$.movements").isArray())
                .andExpect(jsonPath("$.movements[0].referenceNumber").value("F001-00000001"));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/financial-reports/cash-flow/pdf returns 200 OK with application/pdf binary")
    void downloadCashFlowPdfSuccess() throws Exception {
        byte[] mockPdf = "%PDF-1.4 mock pdf content".getBytes();
        when(cashFlowQueryService.exportCashFlowPdf(eq(tenantId), any(), any()))
                .thenReturn(mockPdf);

        mockMvc.perform(get("/api/v1/invoicing/financial-reports/cash-flow/pdf")
                        .header("X-Tenant-Id", tenantId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/financial-reports/cash-flow returns 403 Forbidden without tenant context")
    void getCashFlowMissingTenant() throws Exception {
        mockMvc.perform(get("/api/v1/invoicing/financial-reports/cash-flow"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/financial-reports/cash-flow supports startDate and endDate query parameter aliases")
    void getCashFlowWithDateAliases() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(15);
        LocalDate endDate = LocalDate.now();

        when(cashFlowQueryService.handle(any(GetCashFlowSummaryQuery.class)))
                .thenReturn(sampleSummary);
        when(cashFlowQueryService.getCashFlowMovements(eq(tenantId), any(), any()))
                .thenReturn(List.of(sampleMovement));

        mockMvc.perform(get("/api/v1/invoicing/financial-reports/cash-flow")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.00));
    }
}
