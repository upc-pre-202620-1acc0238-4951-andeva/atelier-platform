package com.andeva.atelier.platform.billing.infrastructure.external.mail.resend;

import com.andeva.atelier.platform.billing.application.internal.outbound.acl.IamTenantValidationAclPort;
import com.andeva.atelier.platform.billing.application.internal.outbound.acl.TenantBillingNotificationGatewayPort;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Resend email adapter implementing {@link TenantBillingNotificationGatewayPort}.
 * Delivers billing lifecycle emails and invoice receipts to workshop administrative accounts
 * using the Resend REST API with safe mock fallbacks for local and test environments.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class ResendBillingNotificationAdapter implements TenantBillingNotificationGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(ResendBillingNotificationAdapter.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final IamTenantValidationAclPort iamTenantValidationAclPort;
    private final String resendApiKey;
    private final String fromEmail;
    private final RestClient restClient;

    public ResendBillingNotificationAdapter(
            IamTenantValidationAclPort iamTenantValidationAclPort,
            @Value("${resend.api-key:resend_key_placeholder}") String resendApiKey,
            @Value("${resend.from-email:billing@atelier.andeva.com}") String fromEmail
    ) {
        this.iamTenantValidationAclPort = Objects.requireNonNull(iamTenantValidationAclPort, "IamTenantValidationAclPort cannot be null");
        this.resendApiKey = resendApiKey;
        this.fromEmail = fromEmail != null ? fromEmail : "billing@atelier.andeva.com";
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void sendSubscriptionActivatedNotification(TenantId tenantId, String planName, Instant periodEnd) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        String email = iamTenantValidationAclPort.getTenantContactEmail(tenantId);
        String subject = "Subscription Activated: " + planName + " — Atelier Platform";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Subscription Activated</h2>
                    <p>Your subscription to <strong>%s</strong> is now active.</p>
                    <p>Current billing cycle ends on: <strong>%s</strong>.</p>
                </div>
                """.formatted(planName, periodEnd != null ? periodEnd.toString() : "N/A");

        sendEmail(email, subject, html);
    }

    @Override
    public void sendSubscriptionRenewedNotification(TenantId tenantId, String planName, Instant newPeriodEnd) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        String email = iamTenantValidationAclPort.getTenantContactEmail(tenantId);
        String subject = "Subscription Renewed: " + planName + " — Atelier Platform";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Subscription Renewed</h2>
                    <p>Your subscription to <strong>%s</strong> has been renewed successfully.</p>
                    <p>Next billing renewal date: <strong>%s</strong>.</p>
                </div>
                """.formatted(planName, newPeriodEnd != null ? newPeriodEnd.toString() : "N/A");

        sendEmail(email, subject, html);
    }

    @Override
    public void sendPaymentFailedNotification(TenantId tenantId, String failureReason, String updatePaymentUrl) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        String email = iamTenantValidationAclPort.getTenantContactEmail(tenantId);
        String subject = "Urgent: Payment Failed — Atelier Platform";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #dc2626;">Payment Failed</h2>
                    <p>We were unable to process your recurring subscription payment.</p>
                    <p><strong>Reason:</strong> %s</p>
                    <div style="margin: 24px 0;">
                        <a href="%s" style="background-color: #dc2626; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">Update Payment Method</a>
                    </div>
                    <p style="color: #6b7280; font-size: 13px;">Please update your payment details within 5 days to avoid service interruption.</p>
                </div>
                """.formatted(failureReason != null ? failureReason : "Card declined", updatePaymentUrl != null ? updatePaymentUrl : "#");

        sendEmail(email, subject, html);
    }

    @Override
    public void sendInvoiceReceipt(TenantId tenantId, SaasInvoice invoice) {
        Objects.requireNonNull(tenantId, "TenantId cannot be null");
        Objects.requireNonNull(invoice, "SaasInvoice cannot be null");
        String email = iamTenantValidationAclPort.getTenantContactEmail(tenantId);
        String subject = "Invoice Receipt #" + invoice.stripeInvoiceId().value() + " — Atelier Platform";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Invoice Receipt</h2>
                    <p>Thank you for your payment!</p>
                    <table style="width: 100%%; border-collapse: collapse; margin: 16px 0;">
                        <tr><td style="padding: 8px 0; color: #6b7280;">Invoice ID:</td><td style="font-weight: bold;">%s</td></tr>
                        <tr><td style="padding: 8px 0; color: #6b7280;">Amount Paid:</td><td style="font-weight: bold;">%s %s</td></tr>
                    </table>
                    <div style="margin: 20px 0;">
                        <a href="%s" style="color: #2563eb; text-decoration: underline;">Download PDF Receipt</a>
                    </div>
                </div>
                """.formatted(
                invoice.stripeInvoiceId().value(),
                invoice.amountPaid().amount(),
                invoice.currency(),
                invoice.invoicePdfUrl() != null ? invoice.invoicePdfUrl() : invoice.hostedInvoiceUrl()
        );

        sendEmail(email, subject, html);
    }

    private void sendEmail(String to, String subject, String html) {
        if (to == null || to.isBlank()) {
            log.warn("Cannot send email: recipient address is empty. Subject: {}", subject);
            return;
        }

        if (resendApiKey == null || resendApiKey.isBlank() || resendApiKey.contains("placeholder") || resendApiKey.contains("mock")) {
            log.info("Email delivery simulated (Mock Resend API Key). Recipient: {}, Subject: {}", to, subject);
            return;
        }

        try {
            Map<String, Object> payload = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "html", html
            );

            restClient.post()
                    .uri(RESEND_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Billing email successfully sent via Resend API to {}", to);
        } catch (Exception e) {
            log.error("Failed to send billing email via Resend API to {}: {}", to, e.getMessage(), e);
        }
    }
}
