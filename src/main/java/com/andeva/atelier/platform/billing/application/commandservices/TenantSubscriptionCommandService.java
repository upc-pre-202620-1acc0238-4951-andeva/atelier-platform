package com.andeva.atelier.platform.billing.application.commandservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.commands.CancelSubscriptionCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.ChangeSubscriptionPlanCommand;
import com.andeva.atelier.platform.billing.domain.model.commands.InitiateCheckoutSessionCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

/**
 * Application Command Service contract for managing workshop tenant subscription contracts.
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantSubscriptionCommandService {

    /**
     * Initializes a hosted Stripe Checkout Session for a tenant and returns the redirect URL.
     *
     * @param command InitiateCheckoutSessionCommand
     * @return checkout URL string
     */
    String handle(InitiateCheckoutSessionCommand command);

    /**
     * Changes the contracted subscription plan (upgrade or downgrade).
     *
     * @param command ChangeSubscriptionPlanCommand
     */
    void handle(ChangeSubscriptionPlanCommand command);

    /**
     * Cancels an active subscription either at period end or immediately.
     *
     * @param command CancelSubscriptionCommand
     */
    void handle(CancelSubscriptionCommand command);

    /**
     * Provisions a free trial subscription for a newly registered workshop tenant.
     *
     * @param tenantId  workshop tenant identifier
     * @param planId    commercial plan identifier to trial
     * @param trialDays number of days for the trial period
     * @return newly provisioned TenantSubscription aggregate
     */
    TenantSubscription handleStartTrial(TenantId tenantId, PlanId planId, int trialDays);

    /**
     * Generates a hosted Stripe Customer Billing Portal session URL for autonomous billing self-management.
     *
     * @param tenantId  workshop tenant identifier
     * @param returnUrl callback URL after portal session ends
     * @return secure customer portal redirect URL
     */
    String handleCreateCustomerPortalSession(TenantId tenantId, String returnUrl);
}
