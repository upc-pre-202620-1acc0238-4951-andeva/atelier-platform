package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.queryservices.SaasInvoiceQueryService;
import com.andeva.atelier.platform.billing.domain.exceptions.SaasInvoiceNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.queries.ListTenantInvoicesQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.interfaces.rest.advice.BillingExceptionHandler;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.SaasInvoiceResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link SaasInvoicesController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SaasInvoicesController Unit Tests")
class SaasInvoicesControllerTest {

    @Mock
    private SaasInvoiceQueryService invoiceQueryService;

    private MockMvc mockMvc;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID otherTenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();

    private TenantId tenantId;
    private SaasInvoice tenantInvoice;
    private SaasInvoice otherTenantInvoice;

    @BeforeEach
    void setUp() {
        tenantId = new TenantId(tenantUuid);
        SaasInvoiceResourceAssembler assembler = new SaasInvoiceResourceAssembler();
        SaasInvoicesController controller = new SaasInvoicesController(invoiceQueryService, assembler);

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
                                "accountant@andeva.pe",
                                "hashedPassword",
                                tenantUuid,
                                Collections.emptyList(),
                                true
                        );
                    }
                })
                .setControllerAdvice(new BillingExceptionHandler())
                .build();

        tenantInvoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                tenantId,
                new StripeInvoiceId("in_test_1001"),
                Money.of(new BigDecimal("129.00"), Currency.USD),
                "https://pay.stripe.com/invoice/in_test_1001/pdf",
                "https://invoice.stripe.com/i/in_test_1001",
                Instant.now()
        );

        otherTenantInvoice = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                new TenantId(otherTenantUuid),
                new StripeInvoiceId("in_other_2002"),
                Money.of(new BigDecimal("49.00"), Currency.USD),
                "https://pay.stripe.com/invoice/in_other_2002/pdf",
                "https://invoice.stripe.com/i/in_other_2002",
                Instant.now()
        );
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices returns tenant invoice summaries")
    void getTenantInvoicesReturns200() throws Exception {
        when(invoiceQueryService.handle(any(ListTenantInvoicesQuery.class))).thenReturn(List.of(tenantInvoice));

        mockMvc.perform(get("/api/v1/billing/invoices")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].stripeInvoiceId").value("in_test_1001"))
                .andExpect(jsonPath("$[0].amountPaid").value(129.00))
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices?status=PAID filters invoices by status")
    void getTenantInvoicesWithStatusFilterReturns200() throws Exception {
        when(invoiceQueryService.handle(any(ListTenantInvoicesQuery.class))).thenReturn(List.of(tenantInvoice));

        mockMvc.perform(get("/api/v1/billing/invoices")
                        .param("status", "PAID")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices/{id} returns detailed invoice when found")
    void getInvoiceByIdFoundReturns200() throws Exception {
        when(invoiceQueryService.findById(tenantInvoice.id())).thenReturn(Optional.of(tenantInvoice));

        mockMvc.perform(get("/api/v1/billing/invoices/" + tenantInvoice.id().value())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(tenantInvoice.id().value().toString()))
                .andExpect(jsonPath("$.stripeInvoiceId").value("in_test_1001"))
                .andExpect(jsonPath("$.amountPaid").value(129.00))
                .andExpect(jsonPath("$.invoicePdfUrl").value("https://pay.stripe.com/invoice/in_test_1001/pdf"));
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices/{id} returns 404 when invoice not found")
    void getInvoiceByIdNotFoundReturns404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(invoiceQueryService.findById(new SaasInvoiceId(unknownId))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/billing/invoices/" + unknownId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("SaaS Invoice Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices/{id} rejects access when invoice belongs to another tenant (403)")
    void getInvoiceByIdOtherTenantThrowsForbidden() throws Exception {
        when(invoiceQueryService.findById(otherTenantInvoice.id())).thenReturn(Optional.of(otherTenantInvoice));

        mockMvc.perform(get("/api/v1/billing/invoices/" + otherTenantInvoice.id().value())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices/{id}/pdf redirects to Stripe PDF with 302 Found")
    void redirectToInvoicePdfReturns302() throws Exception {
        when(invoiceQueryService.findById(tenantInvoice.id())).thenReturn(Optional.of(tenantInvoice));

        mockMvc.perform(get("/api/v1/billing/invoices/" + tenantInvoice.id().value() + "/pdf"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://pay.stripe.com/invoice/in_test_1001/pdf"));
    }

    @Test
    @DisplayName("GET /api/v1/billing/invoices/{id}/pdf returns 404 when invoice has no PDF URL")
    void redirectToInvoicePdfWithoutUrlReturns404() throws Exception {
        SaasInvoice invoiceWithoutPdf = SaasInvoice.recordPaid(
                SubscriptionId.generate(),
                tenantId,
                new StripeInvoiceId("in_test_no_pdf"),
                Money.of(new BigDecimal("99.00"), Currency.USD),
                "",
                "",
                Instant.now()
        );

        when(invoiceQueryService.findById(invoiceWithoutPdf.id())).thenReturn(Optional.of(invoiceWithoutPdf));

        mockMvc.perform(get("/api/v1/billing/invoices/" + invoiceWithoutPdf.id().value() + "/pdf"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("SaaS Invoice Not Found"));
    }
}
