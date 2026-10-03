package com.andeva.atelier.platform.billing.interfaces.rest.controllers;

import com.andeva.atelier.platform.billing.application.commandservices.StripeWebhookCommandService;
import com.andeva.atelier.platform.billing.domain.exceptions.InvalidWebhookSignatureException;
import com.andeva.atelier.platform.billing.domain.model.commands.ProcessStripeWebhookCommand;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.StripeWebhookAcknowledgmentResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Perimeter webhook intake controller processing asynchronous Stripe notifications
 * with HMAC-SHA256 signature verification and Exactly-Once idempotency shielding.
 *
 * @author Joel Huamani Estefanero
 */
@RestController
@RequestMapping("/api/v1/billing/webhooks/stripe")
@Tag(name = "Stripe Webhooks", description = "Asynchronous intake endpoints for Stripe payment and subscription lifecycle webhooks")
public class StripeWebhooksController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhooksController.class);

    private final StripeWebhookCommandService stripeWebhookCommandService;
    private final ObjectMapper objectMapper;

    public StripeWebhooksController(
            StripeWebhookCommandService stripeWebhookCommandService,
            ObjectMapper objectMapper
    ) {
        this.stripeWebhookCommandService = Objects.requireNonNull(stripeWebhookCommandService, "StripeWebhookCommandService cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
    }

    /**
     * Ingests, authenticates, and routes an asynchronous Stripe webhook notification.
     *
     * @param sigHeader raw HMAC-SHA256 signature header (t=...,v1=...)
     * @param rawPayload untouched JSON payload directly from the HTTP request body
     * @return acknowledgment response confirming idempotent receipt
     */
    @Operation(summary = "Process Stripe webhook notification")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook received and processed idempotently"),
            @ApiResponse(responseCode = "400", description = "Bad request / malformed payload"),
            @ApiResponse(responseCode = "401", description = "Unauthorized / Invalid webhook signature"),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity / handling error")
    })
    @PostMapping
    public ResponseEntity<StripeWebhookAcknowledgmentResponse> handleStripeWebhook(
            @RequestHeader(value = "Stripe-Signature", required = false) String sigHeader,
            @RequestBody String rawPayload
    ) {
        if (sigHeader == null || sigHeader.isBlank()) {
            throw new InvalidWebhookSignatureException("Missing Stripe-Signature header");
        }
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException("Empty webhook payload");
        }

        String eventId = extractEventId(rawPayload);

        stripeWebhookCommandService.handle(new ProcessStripeWebhookCommand(rawPayload, sigHeader));

        return ResponseEntity.ok(new StripeWebhookAcknowledgmentResponse(true, eventId, "PROCESSED"));
    }

    private String extractEventId(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode idNode = root.get("id");
            if (idNode != null && !idNode.asText().isBlank()) {
                return idNode.asText();
            }
        } catch (Exception e) {
            log.warn("Could not extract eventId from raw payload, using fallback: {}", e.getMessage());
        }
        return "evt_unknown";
    }
}
