package com.andeva.atelier.platform.billing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload to generate a secure session URL for the Stripe Customer Billing Portal.
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerPortalRequest(
        @NotBlank(message = "{billing.validation.portal.return_url.required}")
        String returnUrl
) {
}
