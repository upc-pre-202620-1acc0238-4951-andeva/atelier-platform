package com.andeva.atelier.platform.billing.application.internal.outbound.acl;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.time.Instant;

/**
 * Outbound port for dispatching billing and subscription email notifications
 * to workshop managers via email providers (e.g. Resend).
 *
 * @author Joel Huamani Estefanero
 */
public interface TenantBillingNotificationGatewayPort {

    /**
     * Sends a welcome notification confirming subscription activation.
     *
     * @param tenantId  workshop tenant identifier
     * @param planName  name of the contracted plan
     * @param periodEnd timestamp when current coverage ends
     */
    void sendSubscriptionActivatedNotification(TenantId tenantId, String planName, Instant periodEnd);

    /**
     * Sends a recurring renewal confirmation email.
     *
     * @param tenantId     workshop tenant identifier
     * @param planName     name of the contracted plan
     * @param newPeriodEnd timestamp when renewed coverage ends
     */
    void sendSubscriptionRenewedNotification(TenantId tenantId, String planName, Instant newPeriodEnd);

    /**
     * Sends an urgent past due notification alerting that recurring payment failed.
     *
     * @param tenantId         workshop tenant identifier
     * @param failureReason    debit rejection reason from Stripe
     * @param updatePaymentUrl URL to Stripe Customer Portal to update card
     */
    void sendPaymentFailedNotification(TenantId tenantId, String failureReason, String updatePaymentUrl);

    /**
     * Sends a digital PDF invoice receipt for accounting records.
     *
     * @param tenantId workshop tenant identifier
     * @param invoice  SaasInvoice receipt aggregate
     */
    void sendInvoiceReceipt(TenantId tenantId, SaasInvoice invoice);
}
