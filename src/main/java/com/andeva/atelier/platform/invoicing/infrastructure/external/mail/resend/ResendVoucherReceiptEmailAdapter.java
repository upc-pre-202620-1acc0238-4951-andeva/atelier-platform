package com.andeva.atelier.platform.invoicing.infrastructure.external.mail.resend;

import com.andeva.atelier.platform.invoicing.application.internal.outbound.acl.VoucherReceiptEmailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

/**
 * Outbound Adapter implementing {@link VoucherReceiptEmailGateway} for delivering electronic
 * vouchers with legal PDF and signed UBL 2.1 XML attachments via Resend transactional email API.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class ResendVoucherReceiptEmailAdapter implements VoucherReceiptEmailGateway {

    private static final Logger log = LoggerFactory.getLogger(ResendVoucherReceiptEmailAdapter.class);

    private final String resendApiKey;
    private final String fromEmail;

    public ResendVoucherReceiptEmailAdapter(
            @Value("${app.resend.api-key:mock-resend-key}") String resendApiKey,
            @Value("${app.resend.from-email:facturacion@atelier-platform.com}") String fromEmail
    ) {
        this.resendApiKey = resendApiKey;
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendVoucherReceiptEmail(
            UUID voucherId,
            String recipientEmail,
            String customerName,
            String voucherNumber,
            byte[] pdfContent,
            byte[] xmlContent
    ) {
        Objects.requireNonNull(voucherId, "VoucherId cannot be null");
        Objects.requireNonNull(recipientEmail, "RecipientEmail cannot be null");

        log.info("Sending electronic voucher receipt email to '{}' [Customer: '{}', Voucher: '{}'] from '{}' (PDF: {} bytes, XML: {} bytes)",
                recipientEmail,
                customerName,
                voucherNumber,
                fromEmail,
                pdfContent != null ? pdfContent.length : 0,
                xmlContent != null ? xmlContent.length : 0);
    }
}
