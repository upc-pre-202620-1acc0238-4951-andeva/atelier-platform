package com.andeva.atelier.platform.invoicing.infrastructure.external.mail.resend;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.VoucherReceiptEmailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Outbound Adapter implementing {@link VoucherReceiptEmailGateway} for delivering electronic
 * vouchers via the Resend transactional email API. The PDF and signed UBL 2.1 XML hosted by
 * Nubefact are attached by URL, so Resend fetches them directly.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class ResendVoucherReceiptEmailAdapter implements VoucherReceiptEmailGateway {

    private static final Logger log = LoggerFactory.getLogger(ResendVoucherReceiptEmailAdapter.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String resendApiKey;
    private final String fromEmail;
    private final RestClient restClient;

    public ResendVoucherReceiptEmailAdapter(
            @Value("${resend.api-key:resend_key_placeholder}") String resendApiKey,
            @Value("${resend.from-email:Atelier <notificaciones@andeva.tech>}") String fromEmail
    ) {
        this.resendApiKey = resendApiKey;
        this.fromEmail = fromEmail;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public void sendVoucherReceiptEmail(
            UUID voucherId,
            String recipientEmail,
            String customerName,
            String voucherNumber,
            String pdfUrl,
            String xmlUrl
    ) {
        Objects.requireNonNull(voucherId, "VoucherId cannot be null");
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("Skipping voucher {} receipt email: customer has no email address", voucherNumber);
            return;
        }

        // Simulated vouchers (PSE in demo mode) have no hosted PDF/XML
        boolean simulated = isBlank(pdfUrl) && isBlank(xmlUrl);
        String subject = (simulated ? "[SIMULADO] " : "") + "Comprobante electrónico " + voucherNumber + " — Atelier";
        String detail = simulated
                ? "<p style=\"color: #b45309; font-size: 14px;\"><strong>Comprobante de demostración (SIMULADO):</strong> no fue enviado a SUNAT y no tiene validez tributaria.</p>"
                : "<p style=\"color: #64748b; font-size: 14px;\">Encontrará adjuntos la representación impresa (PDF) y el archivo XML firmado.</p>";
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2>Gracias por su preferencia</h2>
                    <p>Estimado(a) <strong>%s</strong>, le enviamos el detalle de su comprobante electrónico <strong>%s</strong>.</p>
                    %s
                </div>
                """.formatted(customerName != null ? customerName : "cliente", voucherNumber, detail);

        if (resendApiKey == null || resendApiKey.isBlank() || resendApiKey.contains("placeholder") || resendApiKey.contains("mock")) {
            log.info("[Mock Resend] Voucher {} receipt email to '{}'", voucherNumber, recipientEmail);
            return;
        }

        List<Map<String, String>> attachments = new ArrayList<>();
        if (!isBlank(pdfUrl)) {
            attachments.add(Map.of("filename", voucherNumber + ".pdf", "path", pdfUrl));
        }
        if (!isBlank(xmlUrl)) {
            attachments.add(Map.of("filename", voucherNumber + ".xml", "path", xmlUrl));
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", fromEmail);
        payload.put("to", List.of(recipientEmail));
        payload.put("subject", subject);
        payload.put("html", html);
        if (!attachments.isEmpty()) {
            payload.put("attachments", attachments);
        }

        try {
            restClient.post()
                    .uri(RESEND_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Voucher {} receipt email sent via Resend to '{}'", voucherNumber, recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send voucher {} receipt email via Resend to '{}': {}", voucherNumber, recipientEmail, e.getMessage());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
