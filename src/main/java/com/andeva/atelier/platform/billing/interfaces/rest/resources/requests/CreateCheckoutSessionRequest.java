package com.andeva.atelier.platform.billing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request payload to initiate a hosted Stripe Checkout session for subscription purchase or upgrade.
 *
 * @author Joel Huamani Estefanero
 */
public record CreateCheckoutSessionRequest(
        @NotNull(message = "{billing.validation.checkout.plan_id.required}")
        UUID planId,

        @NotBlank(message = "{billing.validation.checkout.success_url.required}")
        String successUrl,

        @NotBlank(message = "{billing.validation.checkout.cancel_url.required}")
        String cancelUrl
) {
}
