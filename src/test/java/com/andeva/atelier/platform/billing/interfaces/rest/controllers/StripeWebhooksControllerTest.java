package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.commandservices.StripeWebhookCommandService;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import com.andeva.atelier.platform.billing.domain.exceptions.StripeWebhookProcessingException;
import com.andeva.atelier.platform.billing.domain.model.commands.ProcessStripeWebhookCommand;
import com.andeva.atelier.platform.billing.interfaces.rest.advice.BillingExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit test suite for {@link StripeWebhooksController}.
 *
 * @author Joel Huamani Estefanero
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StripeWebhooksController Unit Tests")
class StripeWebhooksControllerTest {

    @Mock
    private StripeWebhookCommandService stripeWebhookCommandService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        StripeWebhooksController controller = new StripeWebhooksController(stripeWebhookCommandService, objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new BillingExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/billing/webhooks/stripe processes event and returns 200 OK")
    void handleValidWebhookReturns200() throws Exception {
        String payload = """
                {
                    "id": "evt_test_checkout_001",
                    "type": "checkout.session.completed",
                    "data": {
                        "object": {
                            "customer": "cus_123"
                        }
                    }
                }
                """;
        String signature = "t=1700000000,v1=abcdef123456";

        mockMvc.perform(post("/api/v1/billing/webhooks/stripe")
                        .header("Stripe-Signature", signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.received").value(true))
                .andExpect(jsonPath("$.eventId").value("evt_test_checkout_001"))
                .andExpect(jsonPath("$.status").value("PROCESSED"));

        verify(stripeWebhookCommandService).handle(any(ProcessStripeWebhookCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/billing/webhooks/stripe returns 401 when Stripe-Signature header is missing")
    void handleWebhookMissingSignatureReturns401() throws Exception {
        String payload = "{\"id\": \"evt_test\"}";

        mockMvc.perform(post("/api/v1/billing/webhooks/stripe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid Webhook Signature"));
    }

    @Test
    @DisplayName("POST /api/v1/billing/webhooks/stripe returns 401 when signature verification fails")
    void handleWebhookInvalidSignatureFailsReturns401() throws Exception {
        String payload = "{\"id\": \"evt_tampered\"}";
        String signature = "t=1700000000,v1=invalid";

        doThrow(new InvalidWebhookSignatureException("HMAC signature mismatch"))
                .when(stripeWebhookCommandService).handle(any());

        mockMvc.perform(post("/api/v1/billing/webhooks/stripe")
                        .header("Stripe-Signature", signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid Webhook Signature"));
    }

    @Test
    @DisplayName("POST /api/v1/billing/webhooks/stripe returns 422 when processing error occurs")
    void handleWebhookProcessingErrorReturns422() throws Exception {
        String payload = "{\"id\": \"evt_processing_err\"}";
        String signature = "t=1700000000,v1=valid";

        doThrow(new StripeWebhookProcessingException("Failed to decode Stripe event JSON", null))
                .when(stripeWebhookCommandService).handle(any());

        mockMvc.perform(post("/api/v1/billing/webhooks/stripe")
                        .header("Stripe-Signature", signature)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Webhook Processing Failed"));
    }

    @Test
    @DisplayName("POST /api/v1/billing/webhooks/stripe returns 400 when payload is empty or blank")
    void handleWebhookEmptyPayloadReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/billing/webhooks/stripe")
                        .header("Stripe-Signature", "t=1700000000,v1=valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid Argument"));
    }
}
