package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.commandservices.TenantSubscriptionCommandService;
import com.andeva.atelier.platform.billing.application.queryservices.SubscriptionPlanQueryService;
import com.andeva.atelier.platform.billing.application.queryservices.TenantSubscriptionQueryService;
import com.andeva.atelier.platform.billing.domain.exceptions.SubscriptionNotFoundException;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.InitiateCheckoutSessionCommand;
import com.andeva.atelier.platform.billing.domain.model.enums.BillingCycle;
import com.andeva.atelier.platform.billing.domain.model.enums.PlanTier;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.queries.GetSubscriptionPlanByIdQuery;
import com.andeva.atelier.platform.billing.domain.model.queries.GetTenantSubscriptionQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.PlanPricing;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.SubscriptionPeriod;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.TenantQuotaLimits;
import com.andeva.atelier.platform.billing.interfaces.rest.advice.BillingExceptionHandler;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CancelSubscriptionRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CreateCheckoutSessionRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.requests.CustomerPortalRequest;
import com.andeva.atelier.platform.billing.interfaces.rest.transform.TenantSubscriptionResourceAssembler;
import com.andeva.atelier.platform.iam.infrastructure.security.model.CustomUserDetails;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link TenantSubscriptionsController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantSubscriptionsController Unit Tests")
class TenantSubscriptionsControllerTest {

    @Mock
    private TenantSubscriptionCommandService subscriptionCommandService;

    @Mock
    private TenantSubscriptionQueryService subscriptionQueryService;

    @Mock
    private SubscriptionPlanQueryService planQueryService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private final UUID tenantUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private TenantId tenantId;
    private TenantSubscription subscription;
    private SubscriptionPlan planGo;
    private SubscriptionPlan planPro;

    @BeforeEach
    void setUp() {
        tenantId = new TenantId(tenantUuid);
        TenantSubscriptionResourceAssembler assembler = new TenantSubscriptionResourceAssembler();
        TenantSubscriptionsController controller = new TenantSubscriptionsController(
                subscriptionCommandService,
                subscriptionQueryService,
                planQueryService,
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
                                "workshop-owner@andeva.pe",
                                "hashedPassword",
                                tenantUuid,
                                Collections.emptyList(),
                                true
                        );
                    }
                })
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

        Instant now = Instant.now();
        subscription = TenantSubscription.activate(
                tenantId,
                planGo.id(),
                new StripeCustomerId("cus_test_123"),
                new StripeSubscriptionId("sub_test_123"),
                new SubscriptionPeriod(now, now.plus(30, ChronoUnit.DAYS))
        );
    }

    @Test
    @DisplayName("GET /api/v1/billing/subscriptions/me returns authenticated tenant subscription")
    void getCurrentTenantSubscriptionReturns200() throws Exception {
        when(subscriptionQueryService.handle(any(GetTenantSubscriptionQuery.class))).thenReturn(Optional.of(subscription));
        when(planQueryService.handle(any(GetSubscriptionPlanByIdQuery.class))).thenReturn(Optional.of(planGo));

        mockMvc.perform(get("/api/v1/billing/subscriptions/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId").value(tenantUuid.toString()))
                .andExpect(jsonPath("$.planName").value("Plan Go"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.isAccessGranted").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/billing/subscriptions/me returns 404 when subscription not found")
    void getCurrentTenantSubscriptionNotFoundReturns404() throws Exception {
        when(subscriptionQueryService.handle(any(GetTenantSubscriptionQuery.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/billing/subscriptions/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Subscription Not Found"));
    }

    @Test
    @DisplayName("POST /api/v1/billing/subscriptions/checkout-session creates Stripe Checkout URL")
    void createCheckoutSessionReturns200() throws Exception {
        CreateCheckoutSessionRequest request = new CreateCheckoutSessionRequest(
                planGo.id().value(),
                "https://app.andeva.pe/success",
                "https://app.andeva.pe/cancel"
        );

        when(subscriptionCommandService.handle(any(InitiateCheckoutSessionCommand.class)))
                .thenReturn("https://checkout.stripe.com/pay/cs_test_123");

        mockMvc.perform(post("/api/v1/billing/subscriptions/checkout-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.com/pay/cs_test_123"))
                .andExpect(jsonPath("$.sessionId").isNotEmpty());

        verify(subscriptionCommandService).handle(any(InitiateCheckoutSessionCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/billing/subscriptions/customer-portal creates Stripe portal URL")
    void createCustomerPortalReturns200() throws Exception {
        CustomerPortalRequest request = new CustomerPortalRequest("https://app.andeva.pe/billing");

        when(subscriptionCommandService.handleCreateCustomerPortalSession(any(), any()))
                .thenReturn("https://billing.stripe.com/p/session/portal_test_123");

        mockMvc.perform(post("/api/v1/billing/subscriptions/customer-portal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portalUrl").value("https://billing.stripe.com/p/session/portal_test_123"));

        verify(subscriptionCommandService).handleCreateCustomerPortalSession(any(), any());
    }


    @Test
    @DisplayName("POST /api/v1/billing/subscriptions/cancel at period end returns 200 OK")
    void cancelSubscriptionAtPeriodEndReturns200() throws Exception {
        CancelSubscriptionRequest request = new CancelSubscriptionRequest(false, "Switching services");

        when(subscriptionQueryService.handle(any(GetTenantSubscriptionQuery.class))).thenReturn(Optional.of(subscription));
        when(planQueryService.handle(any(GetSubscriptionPlanByIdQuery.class))).thenReturn(Optional.of(planGo));

        mockMvc.perform(post("/api/v1/billing/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(subscriptionCommandService).handle(any(CancelSubscriptionCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/billing/subscriptions/cancel immediately returns 204 No Content")
    void cancelSubscriptionImmediatelyReturns204() throws Exception {
        CancelSubscriptionRequest request = new CancelSubscriptionRequest(true, "Shutting down workshop");

        when(subscriptionQueryService.handle(any(GetTenantSubscriptionQuery.class))).thenReturn(Optional.of(subscription));

        mockMvc.perform(post("/api/v1/billing/subscriptions/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(subscriptionCommandService).handle(any(CancelSubscriptionCommand.class));
    }
}
