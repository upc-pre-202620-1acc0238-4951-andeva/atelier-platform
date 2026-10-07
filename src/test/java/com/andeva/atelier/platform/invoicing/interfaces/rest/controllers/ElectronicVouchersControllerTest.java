package com.andeva.atelier.platform.invoicing.interfaces.rest.controllers;

import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.invoicing.application.commandservices.ElectronicVoucherCommandService;
import com.andeva.atelier.platform.invoicing.application.queryservices.ElectronicVoucherQueryService;
import com.andeva.atelier.platform.invoicing.domain.exceptions.VoucherNotFoundException;
import com.andeva.atelier.platform.invoicing.domain.model.aggregates.ElectronicVoucher;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueCreditNoteCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.IssueElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.commands.VoidElectronicVoucherCommand;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.enums.*;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVoucherByIdQuery;
import com.andeva.atelier.platform.invoicing.domain.model.queries.GetVouchersByTenantQuery;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.*;
import com.andeva.atelier.platform.invoicing.interfaces.acl.InvoicingContextFacade;
import com.andeva.atelier.platform.invoicing.interfaces.rest.advice.InvoicingExceptionHandler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.assemblers.ElectronicVoucherResourceAssembler;
import com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests.*;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.BranchId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.CustomerId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TaxId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit test suite for {@link ElectronicVouchersController}.
 * Verifies REST endpoints for electronic voucher issuance, credit note issuance,
 * retrieval by ID, multi-tenant queries, work order lookups, and voiding.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ElectronicVouchersController Unit Tests")
class ElectronicVouchersControllerTest {

    @Mock
    private ElectronicVoucherCommandService voucherCommandService;

    @Mock
    private ElectronicVoucherQueryService voucherQueryService;

    @Mock
    private InvoicingContextFacade invoicingContextFacade;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private TenantId tenantId;
    private BranchId branchId;
    private CustomerId customerId;
    private ElectronicVoucher sampleVoucher;

    @BeforeEach
    void setUp() {
        ElectronicVoucherResourceAssembler assembler = new ElectronicVoucherResourceAssembler();
        ElectronicVouchersController controller = new ElectronicVouchersController(
                voucherCommandService,
                voucherQueryService,
                invoicingContextFacade,
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
        customerId = CustomerId.generate();

        TaxCalculation taxCalc = TaxCalculation.of(
                Money.soles(100.00),
                Money.soles(18.00),
                Money.soles(118.00)
        );

        CustomerFiscalInfo customer = CustomerFiscalInfo.of(
                TaxId.ruc("20100070970"),
                "Corporacion Los Andes S.A.C.",
                "Av. Primavera 123, Lima",
                DocumentType.RUC
        );

        VoucherId voucherId = VoucherId.generate();
        VoucherLine line = VoucherLine.create(
                null,
                voucherId,
                UUID.randomUUID(),
                VoucherItemType.SERVICE,
                "Servicio de afinamiento de motor",
                Quantity.ofUnits(1),
                Money.soles(118.00)
        );

        sampleVoucher = ElectronicVoucher.issue(
                tenantId,
                branchId,
                customerId,
                null,
                VoucherType.FACTURA,
                new VoucherSerie("F001"),
                new VoucherNumber(1),
                customer,
                Currency.PEN,
                taxCalc,
                List.of(line)
        );
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/vouchers issues voucher and returns 201 Created")
    void issueVoucherSuccess() throws Exception {
        when(voucherCommandService.handle(any(IssueElectronicVoucherCommand.class))).thenReturn(sampleVoucher);

        IssueVoucherRequest request = new IssueVoucherRequest(
                branchId.value(),
                customerId.value(),
                UUID.randomUUID(),
                "01",
                "F001",
                new CustomerFiscalInfoRequest("20100070970", "Corporacion Los Andes S.A.C.", "Av. Primavera 123", "6"),
                "PEN",
                List.of(new VoucherLineRequest(UUID.randomUUID(), "SERVICE", "Servicio de afinamiento de motor", new BigDecimal("1.000"), new BigDecimal("118.00")))
        );

        mockMvc.perform(post("/api/v1/invoicing/vouchers")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(sampleVoucher.getId().value().toString()))
                .andExpect(jsonPath("$.serie").value("F001"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.voucherType").value("01"));

        verify(voucherCommandService).handle(any(IssueElectronicVoucherCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/vouchers returns 400 Bad Request on invalid request body")
    void issueVoucherValidationFailure() throws Exception {
        IssueVoucherRequest invalidRequest = new IssueVoucherRequest(
                null,
                null,
                null,
                "invalid",
                "INVALID",
                null,
                "EUR",
                List.of()
        );

        mockMvc.perform(post("/api/v1/invoicing/vouchers")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/vouchers returns 403 Forbidden when no tenant context is provided")
    void issueVoucherMissingTenantContext() throws Exception {
        IssueVoucherRequest request = new IssueVoucherRequest(
                branchId.value(),
                customerId.value(),
                null,
                "01",
                "F001",
                new CustomerFiscalInfoRequest("20100070970", "Corporacion Los Andes S.A.C.", "Av. Primavera 123", "6"),
                "PEN",
                List.of(new VoucherLineRequest(UUID.randomUUID(), "SERVICE", "Servicio", BigDecimal.ONE, new BigDecimal("100.00")))
        );

        mockMvc.perform(post("/api/v1/invoicing/vouchers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/vouchers/credit-notes issues credit note and returns 201 Created")
    void issueCreditNoteSuccess() throws Exception {
        when(voucherCommandService.handle(any(IssueCreditNoteCommand.class))).thenReturn(sampleVoucher);

        IssueCreditNoteRequest request = new IssueCreditNoteRequest(
                branchId.value(),
                customerId.value(),
                sampleVoucher.getId().value(),
                "FC01",
                "ANULACION_DE_LA_OPERACION",
                "Anulacion total de la factura por desistimiento del cliente",
                List.of(new VoucherLineRequest(UUID.randomUUID(), "SERVICE", "Servicio de afinamiento de motor", new BigDecimal("1.000"), new BigDecimal("118.00")))
        );

        mockMvc.perform(post("/api/v1/invoicing/vouchers/credit-notes")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(sampleVoucher.getId().value().toString()));

        verify(voucherCommandService).handle(any(IssueCreditNoteCommand.class));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/vouchers/{id} returns 200 OK when found")
    void getByIdFound() throws Exception {
        when(voucherQueryService.handle(any(GetVoucherByIdQuery.class))).thenReturn(Optional.of(sampleVoucher));

        mockMvc.perform(get("/api/v1/invoicing/vouchers/{id}", sampleVoucher.getId().value())
                        .header("X-Tenant-Id", tenantId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleVoucher.getId().value().toString()))
                .andExpect(jsonPath("$.serie").value("F001"))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/vouchers/{id} returns 404 Not Found when absent")
    void getByIdNotFound() throws Exception {
        when(voucherQueryService.handle(any(GetVoucherByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/invoicing/vouchers/{id}", UUID.randomUUID())
                        .header("X-Tenant-Id", tenantId.value().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/vouchers returns 200 OK with list of vouchers")
    void getVouchersByTenantSuccess() throws Exception {
        when(voucherQueryService.handle(any(GetVouchersByTenantQuery.class))).thenReturn(List.of(sampleVoucher));

        mockMvc.perform(get("/api/v1/invoicing/vouchers")
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .param("series", "F001")
                        .param("status", "ISSUED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].serie").value("F001"));
    }

    @Test
    @DisplayName("GET /api/v1/invoicing/vouchers/work-order/{workOrderId} returns 200 OK")
    void getByWorkOrderSuccess() throws Exception {
        UUID workOrderId = UUID.randomUUID();
        when(voucherQueryService.getVouchersByWorkOrderId(WorkOrderId.of(workOrderId))).thenReturn(List.of(sampleVoucher));

        mockMvc.perform(get("/api/v1/invoicing/vouchers/work-order/{workOrderId}", workOrderId)
                        .header("X-Tenant-Id", tenantId.value().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].serie").value("F001"));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/vouchers/{id}/void voids voucher and returns 200 OK")
    void voidVoucherSuccess() throws Exception {
        when(voucherCommandService.handle(any(VoidElectronicVoucherCommand.class))).thenReturn(sampleVoucher);

        VoidVoucherRequest request = new VoidVoucherRequest("Cliente cancelo orden de trabajo");

        mockMvc.perform(post("/api/v1/invoicing/vouchers/{id}/void", sampleVoucher.getId().value())
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sampleVoucher.getId().value().toString()));

        verify(voucherCommandService).handle(any(VoidElectronicVoucherCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/invoicing/vouchers/{id}/void returns 400 Bad Request on blank reason")
    void voidVoucherBlankReason() throws Exception {
        VoidVoucherRequest request = new VoidVoucherRequest("   ");

        mockMvc.perform(post("/api/v1/invoicing/vouchers/{id}/void", sampleVoucher.getId().value())
                        .header("X-Tenant-Id", tenantId.value().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
