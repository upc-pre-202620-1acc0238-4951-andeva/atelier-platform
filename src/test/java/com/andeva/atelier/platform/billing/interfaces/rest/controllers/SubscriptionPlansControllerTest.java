package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.commandservices.SubscriptionPlanCommandService;
import com.andeva.atelier.platform.billing.application.queryservices.SubscriptionPlanQueryService;
import com.andeva.atelier.platform.billing.domain.exceptions.PlanNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.commands.CreateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.UpdateSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.ListActivePlansQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.interfaces.acl.dto.TenantQuotaLimitsDto;
import com.andeva.atelier.platform.billing.interfaces.rest.advice.BillingExceptionHandler;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CreateSubscriptionPlanRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.UpdateSubscriptionPlanRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.PlanFeatureResourceAssembler;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.SubscriptionPlanResourceAssembler;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link SubscriptionPlansController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionPlansController Unit Tests")
class SubscriptionPlansControllerTest {

    @Mock
    private SubscriptionPlanCommandService planCommandService;

    @Mock
    private SubscriptionPlanQueryService planQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private SubscriptionPlan planGo;
    private SubscriptionPlan planPro;

    @BeforeEach
    void setUp() {
        SubscriptionPlanResourceAssembler assembler = new SubscriptionPlanResourceAssembler(new PlanFeatureResourceAssembler());
        SubscriptionPlansController controller = new SubscriptionPlansController(planCommandService, planQueryService, assembler);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new BillingExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        planGo = SubscriptionPlan.create(
                new StripePriceId("price_go_monthly"),
                "Plan Go",
                PlanTier.GO,
                new PlanPricing(Money.of(new BigDecimal("49.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.goPlanPreset()
        );

        planPro = SubscriptionPlan.create(
                new StripePriceId("price_pro_monthly"),
                "Plan Pro",
                PlanTier.PRO,
                new PlanPricing(Money.of(new BigDecimal("129.00"), Currency.USD), BillingCycle.MONTHLY),
                TenantQuotaLimits.proPlanPreset()
        );
    }

    @Test
    @DisplayName("GET /api/v1/billing/plans returns active plans catalog")
    void getAllActivePlansReturns200() throws Exception {
        when(planQueryService.handle(any(ListActivePlansQuery.class))).thenReturn(List.of(planGo, planPro));

        mockMvc.perform(get("/api/v1/billing/plans")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Plan Go"))
                .andExpect(jsonPath("$[0].tier").value("GO"))
                .andExpect(jsonPath("$[0].price").value(49.00))
                .andExpect(jsonPath("$[1].name").value("Plan Pro"))
                .andExpect(jsonPath("$[1].tier").value("PRO"));
    }

    @Test
    @DisplayName("GET /api/v1/billing/plans?billingCycle=MONTHLY filters plans by billing cycle")
    void getAllActivePlansWithBillingCycleFilter() throws Exception {
        when(planQueryService.handle(any(ListActivePlansQuery.class))).thenReturn(List.of(planGo, planPro));

        mockMvc.perform(get("/api/v1/billing/plans")
                        .param("billingCycle", "MONTHLY")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/billing/plans/{id} returns single plan resource when found")
    void getPlanByIdFoundReturns200() throws Exception {
        when(planQueryService.handle(any(GetSubscriptionPlanByIdQuery.class))).thenReturn(Optional.of(planGo));

        mockMvc.perform(get("/api/v1/billing/plans/" + planGo.id().value())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(planGo.id().value().toString()))
                .andExpect(jsonPath("$.name").value("Plan Go"))
                .andExpect(jsonPath("$.stripePriceId").value("price_go_monthly"));
    }

    @Test
    @DisplayName("GET /api/v1/billing/plans/{id} returns 404 ProblemDetail when plan not found")
    void getPlanByIdNotFoundReturns404() throws Exception {
        UUID unknownId = UUID.randomUUID();
        when(planQueryService.handle(any(GetSubscriptionPlanByIdQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/billing/plans/" + unknownId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Plan Not Found"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("POST /api/v1/billing/plans creates plan and returns 201 Created with Location")
    void createPlanReturns201Created() throws Exception {
        TenantQuotaLimitsDto quotaLimitsDto = new TenantQuotaLimitsDto(
                1, 5, 0, 10, 0, false, false, false, 100, false, false
        );

        CreateSubscriptionPlanRequest request = new CreateSubscriptionPlanRequest(
                "price_go_monthly",
                "Plan Go",
                "GO",
                new BigDecimal("49.00"),
                "USD",
                "MONTHLY",
                quotaLimitsDto
        );

        when(planCommandService.handle(any(CreateSubscriptionPlanCommand.class))).thenReturn(planGo.id());
        when(planQueryService.handle(any(GetSubscriptionPlanByIdQuery.class))).thenReturn(Optional.of(planGo));

        mockMvc.perform(post("/api/v1/billing/plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/billing/plans/" + planGo.id().value()))
                .andExpect(jsonPath("$.name").value("Plan Go"))
                .andExpect(jsonPath("$.tier").value("GO"));

        verify(planCommandService).handle(any(CreateSubscriptionPlanCommand.class));
    }

    @Test
    @DisplayName("PUT /api/v1/billing/plans/{id} updates plan details and returns 200 OK")
    void updatePlanReturns200Ok() throws Exception {
        TenantQuotaLimitsDto quotaLimitsDto = new TenantQuotaLimitsDto(
                2, 10, 5, 100, 0, false, false, false, 300, true, false
        );

        UpdateSubscriptionPlanRequest request = new UpdateSubscriptionPlanRequest(
                "Plan Go Updated",
                new BigDecimal("59.00"),
                "MONTHLY",
                quotaLimitsDto,
                true
        );

        when(planQueryService.handle(any(GetSubscriptionPlanByIdQuery.class)))
                .thenReturn(Optional.of(planGo))
                .thenReturn(Optional.of(planGo));

        mockMvc.perform(put("/api/v1/billing/plans/" + planGo.id().value())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Plan Go"));

        verify(planCommandService).handle(any(UpdateSubscriptionPlanCommand.class));
        verify(planCommandService).handleActivate(planGo.id());
    }
}
