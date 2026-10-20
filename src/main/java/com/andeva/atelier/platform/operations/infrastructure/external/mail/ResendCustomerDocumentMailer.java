package com.andeva.atelier.platform.operations.infrastructure.external.mail;

import com.andeva.atelier.platform.operations.application.internal.outbound.acl.CustomerDocumentMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Emails customer documents through the Resend HTTP API with the PDF as an inline attachment.
 */
@Component
public class ResendCustomerDocumentMailer implements CustomerDocumentMailer {

    private static final Logger log = LoggerFactory.getLogger(ResendCustomerDocumentMailer.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String resendApiKey;
    private final String fromEmail;
    private final RestClient restClient;

    public ResendCustomerDocumentMailer(
            @Value("${resend.api-key:resend_key_placeholder}") String resendApiKey,
            @Value("${resend.from-email:Atelier <notificaciones@andeva.tech>}") String fromEmail
    ) {
        this.resendApiKey = resendApiKey;
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public boolean send(String recipientEmail, String subject, String html, String fileName, byte[] pdf) {
        return deliver(recipientEmail, subject, html, fileName, pdf);
    }

    @Override
    public boolean sendWithoutAttachment(String recipientEmail, String subject, String html) {
        return deliver(recipientEmail, subject, html, null, null);
    }

    private boolean deliver(String recipientEmail, String subject, String html, String fileName, byte[] pdf) {
        if (resendApiKey == null || resendApiKey.isBlank() || resendApiKey.contains("placeholder") || resendApiKey.contains("mock")) {
            log.info("[Mock Resend] '{}' to '{}'", subject, recipientEmail);
            return true;
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", fromEmail);
        payload.put("to", List.of(recipientEmail));
        payload.put("subject", subject);
        payload.put("html", html);
        if (pdf != null) {
            payload.put("attachments", List.of(Map.of("filename", fileName, "content", Base64.getEncoder().encodeToString(pdf))));
        }

        try {
            restClient.post()
                    .uri(RESEND_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Email '{}' sent via Resend to '{}'", subject, recipientEmail);
            return true;
        } catch (Exception e) {
            log.error("Failed to send email '{}' via Resend to '{}': {}", subject, recipientEmail, e.getMessage());
            return false;
        }
    }
}
