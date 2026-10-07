package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.commandservices.VoucherPaymentCommandService;
import com.andeva.atelier.platform.invoicing.application.queryservices.VoucherPaymentQueryService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherPayment;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentMethod;
import com.andeva.atelier.platform.invoicing.domain.model.enums.PaymentStatus;
import com.andeva.atelier.platform.invoicing.domain.model.ids.PaymentId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherPaymentsQuery;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import org.mockito.Mockito;
import java.util.Optional;

import com.andeva.atelier.platform.invoicing.interfaces.rest.advice.InvoicingExceptionHandler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.VoucherPaymentResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.RegisterPaymentRequest;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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
 * Unit test suite for {@link VoucherPaymentsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VoucherPaymentsController Unit Tests")
class VoucherPaymentsControllerTest {

    @Mock
    private VoucherPaymentCommandService paymentCommandService;

    @Mock
    private VoucherPaymentQueryService paymentQueryService;

    @Mock
    private ElectronicVoucherQueryService voucherQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TenantId tenantId;
    private BranchId branchId;
    private VoucherId voucherId;
    private VoucherPayment samplePayment;

    @BeforeEach
    void setUp() {
        VoucherPaymentResourceAssembler assembler = new VoucherPaymentResourceAssembler();
        VoucherPaymentsController controller = new VoucherPaymentsController(
                paymentCommandService,
                paymentQueryService,
                voucherQueryService,
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
                        "cashier@andeva.pe",
                        "hash",
                        UUID.fromString(headerTenantId),
                        List.of(new SimpleGrantedAuthority("ROLE_CASHIER")),
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
        voucherId = VoucherId.generate();

        samplePayment = VoucherPayment.record(
                PaymentId.generate(),
                voucherId,
                tenantId,
                branchId,
                Money.soles(100.00),
                PaymentMethod.DIGITAL_WALLET_YAPE,
                "YAPE-REF-12345"
        );
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/payments registers payment and returns 201 Created")
    void registerPaymentSuccess() throws Exception {
        when(paymentCommandService.handle(any())).thenReturn(samplePayment);

        RegisterPaymentRequest request = new RegisterPaymentRequest(
                voucherId.value(),
                branchId.value(),
                new BigDecimal("100.00"),
                "PEN",
                "DIGITAL_WALLET_YAPE",
                "YAPE-REF-12345"
        );

        mockMvc.perform(post("/api/v1/invoicing/payments")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(samplePayment.getId().value().toString()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.currency").value("PEN"))
                .andExpect(jsonPath("$.paymentMethod").value("DIGITAL_WALLET_YAPE"))
                .andExpect(jsonPath("$.status").value(PaymentStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.transactionReference").value("YAPE-REF-12345"));

        verify(paymentCommandService).handle(any());
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/payments returns 400 Bad Request on invalid request")
    void registerPaymentInvalidPayload() throws Exception {
        RegisterPaymentRequest request = new RegisterPaymentRequest(
                null,
                null,
                BigDecimal.ZERO, // Min 0.01
                "PEN",
                "",
                ""
        );

        mockMvc.perform(post("/api/v1/invoicing/payments")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/payments returns 403 Forbidden without tenant context")
    void registerPaymentMissingTenant() throws Exception {
        RegisterPaymentRequest request = new RegisterPaymentRequest(
                voucherId.value(),
                branchId.value(),
                new BigDecimal("50.00"),
                "PEN",
                "CASH",
                "REC-001"
        );

        mockMvc.perform(post("/api/v1/invoicing/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/payments/voucher/{voucherId} returns 200 OK")
    void getPaymentsByVoucherSuccess() throws Exception {
        ElectronicVoucher mockVoucher = Mockito.mock(ElectronicVoucher.class);
        when(mockVoucher.getTenantId()).thenReturn(tenantId);
        when(voucherQueryService.handle(any(GetVoucherByIdQuery.class))).thenReturn(Optional.of(mockVoucher));
        when(paymentQueryService.handle(any(GetVoucherPaymentsQuery.class))).thenReturn(List.of(samplePayment));

        mockMvc.perform(get("/api/v1/invoicing/payments/voucher/{voucherId}", voucherId.value())
                        .header("X-Tenant-Id", tenantId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(samplePayment.getId().value().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/payments/branch/{branchId}/daily returns 200 OK")
    void getDailyReconciliationSuccess() throws Exception {
        when(paymentQueryService.getDailyPaymentsByBranch(eq(branchId), any()))
                .thenReturn(List.of(samplePayment));

        mockMvc.perform(get("/api/v1/invoicing/payments/branch/{branchId}/daily", branchId.value())
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchId").value(branchId.value().toString()))
                .andExpect(jsonPath("$.paymentCount").value(1))
                .andExpect(jsonPath("$.totalCollected").value(100.00))
                .andExpect(jsonPath("$.currency").value("PEN"))
                .andExpect(jsonPath("$.breakdownByMethod.DIGITAL_WALLET_YAPE").value(100.00));
    }
}
