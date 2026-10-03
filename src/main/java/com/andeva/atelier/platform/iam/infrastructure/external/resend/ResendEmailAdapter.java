package com.andeva.atelier.platform.iam.infrastructure.external.resend;

import com.andeva.atelier.platform.iam.application.internal.outbound.acl.ResendEmailService;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.EmailAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Outbound adapter implementing {@link ResendEmailService} using the official Resend HTTPS API.
 * Provides resilient fallback logging in development/test profiles when credentials are placeholders.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class ResendEmailAdapter implements ResendEmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailAdapter.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String apiKey;
    private final String fromEmail;
    private final RestClient restClient;

    public ResendEmailAdapter(
            @Value("${resend.api-key:resend_key_placeholder}") String apiKey,
            @Value("${resend.from-email:notifications@atelier.andeva.com}") String fromEmail) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void sendVerificationEmail(EmailAddress recipient, String otpCode) {
        Objects.requireNonNull(recipient, "Recipient email cannot be null");
        Objects.requireNonNull(otpCode, "OTP code cannot be null");

        String subject = "Verify your Atelier Platform Account";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Welcome to Atelier Platform</h2>
                    <p>Your one-time email verification code is:</p>
                    <div style="font-size: 28px; font-weight: bold; letter-spacing: 4px; padding: 16px; background-color: #f4f6f8; border-radius: 8px; text-align: center; color: #1e293b;">
                        %s
                    </div>
                    <p style="margin-top: 20px; color: #64748b; font-size: 14px;">This code will expire in 15 minutes. If you did not request this, please disregard this email.</p>
                </div>
                """.formatted(otpCode);

        sendEmail(recipient.value(), subject, html);
    }

    @Override
    public void sendPasswordResetEmail(EmailAddress recipient, String resetToken) {
        Objects.requireNonNull(recipient, "Recipient email cannot be null");
        Objects.requireNonNull(resetToken, "Reset token cannot be null");

        String resetUrl = "https://app.atelier.pe/auth/reset-password?token=" + resetToken;
        String subject = "Reset your Atelier Platform Password";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Password Reset Request</h2>
                    <p>We received a request to reset your Atelier Platform password. Click the button below to proceed:</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">Reset Password</a>
                    </div>
                    <p style="color: #64748b; font-size: 14px;">Or copy and paste this link in your browser: %s</p>
                    <p style="color: #94a3b8; font-size: 12px;">This link will expire in 1 hour.</p>
                </div>
                """.formatted(resetUrl, resetUrl);

        sendEmail(recipient.value(), subject, html);
    }

    @Override
    public void sendStaffInvitationEmail(EmailAddress recipient, String tenantName, String inviteToken) {
        Objects.requireNonNull(recipient, "Recipient email cannot be null");
        Objects.requireNonNull(tenantName, "Tenant name cannot be null");
        Objects.requireNonNull(inviteToken, "Invite token cannot be null");

        String inviteUrl = "https://app.atelier.pe/auth/accept-invitation?token=" + inviteToken;
        String subject = "You've been invited to join " + tenantName + " on Atelier";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Join Workshop Team</h2>
                    <p>You have been officially invited to join <strong>%s</strong> on Atelier Platform.</p>
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #059669; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold;">Accept Invitation</a>
                    </div>
                    <p style="color: #64748b; font-size: 14px;">This invitation link will expire in 7 days.</p>
                </div>
                """.formatted(tenantName, inviteUrl);

        sendEmail(recipient.value(), subject, html);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isBlank() || apiKey.contains("placeholder") || apiKey.equals("dummy-key")) {
            log.info("[Mock Resend] Dispatched email to '{}' with subject '{}'", to, subject);
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(to),
                    "subject", subject,
                    "html", htmlContent
            );

            restClient.post()
                    .uri(RESEND_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Transactional email successfully dispatched to '{}' via Resend", to);
        } catch (Exception e) {
            log.error("Failed to send transactional email to '{}' via Resend: {}", to, e.getMessage());
        }
    }
}
