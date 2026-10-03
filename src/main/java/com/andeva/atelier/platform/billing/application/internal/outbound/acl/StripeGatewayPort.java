package com.andeva.atelier.platform.billing.application.internal.outbound.acl;

import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Outbound port defining operations delegated to the external Stripe payment gateway.
 * Keeps external third-party SDK dependencies strictly decoupled from domain and application layers.
 *
 * @author Joel Huamani Estefanero
 */
public interface StripeGatewayPort {

    /**
     * Creates a hosted Stripe Checkout Session for initiating or upgrading a subscription.
     *
     * @param tenantId   workshop tenant identifier
     * @param planId     commercial plan identifier
     * @param priceId    Stripe recurring price identifier
     * @param customerId Stripe customer identifier (optional, can be null for first-time checkout)
     * @param successUrl URL to redirect after successful checkout
     * @param cancelUrl  URL to redirect if checkout is aborted
     * @return hosted Stripe Checkout Session URL
     */
    String createCheckoutSession(
            TenantId tenantId,
            PlanId planId,
            StripePriceId priceId,
            StripeCustomerId customerId,
            String successUrl,
            String cancelUrl
    );

    /**
     * Creates a hosted Stripe Customer Portal Session for managing payment methods and invoices.
     *
     * @param customerId Stripe customer identifier
     * @param returnUrl  URL to return to after customer finishes in the portal
     * @return hosted Stripe Customer Portal URL
     */
    String createCustomerPortalSession(StripeCustomerId customerId, String returnUrl);

    /**
     * Creates a Stripe Customer object on the payment gateway.
     *
     * @param tenantId     workshop tenant identifier
     * @param businessName legal or trade name of the workshop
     * @param email        contact billing email
     * @return newly provisioned StripeCustomerId
     */
    StripeCustomerId createCustomer(TenantId tenantId, String businessName, String email);
}
