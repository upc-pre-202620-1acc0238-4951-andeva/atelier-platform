package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

/**
 * REST response providing the Stripe Customer Portal URL for self-service billing management.
 *
 * @author Joel Huamani Estefanero
 */
public record CustomerPortalResponse(
        String portalUrl
) {
}
