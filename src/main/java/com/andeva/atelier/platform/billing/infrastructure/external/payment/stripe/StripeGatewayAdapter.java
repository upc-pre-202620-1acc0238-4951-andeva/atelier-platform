package com.andeva.atelier.platform.billing.infrastructure.external.payment.stripe;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.StripeGatewayPort;
import com.andeva.atelier.platform.billing.domain.exceptions.StripeIntegrationException;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeCustomerId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Outbound adapter for Stripe Payment Gateway implementing {@link StripeGatewayPort}.
 * Wraps official {@code stripe-java} SDK calls, isolating external payment APIs from core domain logic.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class StripeGatewayAdapter implements StripeGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(StripeGatewayAdapter.class);

    private final StripeClient stripeClient;

    @org.springframework.beans.factory.annotation.Autowired
    public StripeGatewayAdapter(@Value("${stripe.api-key:sk_test_mock_key}") String apiKey) {
        this(new StripeClient(apiKey != null && !apiKey.isBlank() ? apiKey : "sk_test_mock_key"));
    }

    public StripeGatewayAdapter(StripeClient stripeClient) {
        this.stripeClient = Objects.requireNonNull(stripeClient, "StripeClient cannot be null");
    }

    @Override
    public String createCheckoutSession(
            TenantId tenantId,
            PlanId planId,
            StripePriceId priceId,
            StripeCustomerId customerId,
            String successUrl,
            String cancelUrl
    ) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(planId, "PlanId cannot be null");
        Objects.requireNonNull(priceId, "StripePriceId cannot be null");

        try {
            SessionCreateParams.Builder builder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(tenantId.value().toString())
                    .putMetadata("tenant_id", tenantId.value().toString())
                    .putMetadata("plan_id", planId.value().toString())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPrice(priceId.value())
                                    .setQuantity(1L)
                                    .build()
                    );

            if (customerId != null && !customerId.value().isBlank()) {
                builder.setCustomer(customerId.value());
            }

            SessionCreateParams params = builder.build();
            log.info("Creating Stripe Checkout Session for tenant {} and plan {}", tenantId.value(), planId.value());
            return stripeClient.checkout().sessions().create(params).getUrl();

        } catch (StripeException ex) {
            log.error("Stripe error creating checkout session for tenant {}: {}", tenantId.value(), ex.getMessage(), ex);
            throw new StripeIntegrationException("Failed to create Stripe Checkout Session: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error creating checkout session for tenant {}: {}", tenantId.value(), ex.getMessage(), ex);
            throw new StripeIntegrationException("Error communicating with Stripe payment gateway: " + ex.getMessage(), ex);
        }
    }

    @Override
    public String createCustomerPortalSession(StripeCustomerId customerId, String returnUrl) {
        Objects.requireNonNull(customerId, "StripeCustomerId cannot be null");

        try {
            com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams.builder()
                    .setCustomer(customerId.value())
                    .setReturnUrl(returnUrl)
                    .build();

            log.info("Creating Stripe Customer Portal Session for customer {}", customerId.value());
            return stripeClient.billingPortal().sessions().create(params).getUrl();

        } catch (StripeException ex) {
            log.error("Stripe error creating customer portal session for customer {}: {}", customerId.value(), ex.getMessage(), ex);
            throw new StripeIntegrationException("Failed to create Stripe Customer Portal Session: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error creating customer portal session for customer {}: {}", customerId.value(), ex.getMessage(), ex);
            throw new StripeIntegrationException("Error communicating with Stripe portal gateway: " + ex.getMessage(), ex);
        }
    }

    @Override
    public StripeCustomerId createCustomer(TenantId tenantId, String businessName, String email) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");

        try {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setName(businessName != null ? businessName : "Workshop " + tenantId.value())
                    .setEmail(email)
                    .putMetadata("tenant_id", tenantId.value().toString())
                    .build();

            log.info("Creating Stripe Customer for tenant {} ({})", tenantId.value(), email);
            Customer customer = stripeClient.customers().create(params);
            return new StripeCustomerId(customer.getId());

        } catch (StripeException ex) {
            log.error("Stripe error provisioning customer for tenant {}: {}", tenantId.value(), ex.getMessage(), ex);
            throw new StripeIntegrationException("Failed to provision Stripe customer: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error provisioning customer for tenant {}: {}", tenantId.value(), ex.getMessage(), ex);
            throw new StripeIntegrationException("Error communicating with Stripe customer API: " + ex.getMessage(), ex);
        }
    }
}
