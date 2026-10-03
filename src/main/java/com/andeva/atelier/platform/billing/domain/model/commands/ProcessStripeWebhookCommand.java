package com.andeva.atelier.platform.billing.domain.model.commands;

import java.util.Objects;

/**
 * Command carrying the raw JSON payload and signature header of an incoming Stripe webhook.
 *
 * @author Joel Huamani Estefanero
 */
public record ProcessStripeWebhookCommand(
        String payload,
        String signatureHeader
) {

    public ProcessStripeWebhookCommand {
        Objects.requireNonNull(payload, "Payload cannot be null");
        Objects.requireNonNull(signatureHeader, "Signature header cannot be null");
    }
}
