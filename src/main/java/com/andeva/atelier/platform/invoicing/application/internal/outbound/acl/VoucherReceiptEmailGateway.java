package com.andeva.atelier.platform.invoicing.application.internal.outbound.acl;

import java.util.UUID;

/**
 * Outbound ACL Port for emailing electronic tax receipts (PDF and XML)
 * to workshop clients via transactional email provider (Resend).
 *
 * @author Joel Huamani Estefanero
 */
public interface VoucherReceiptEmailGateway {

    /**
     * Sends transactional email containing legal receipt PDF and UBL 2.1 XML attachments.
     */
    void sendVoucherReceiptEmail(
            UUID voucherId,
            String recipientEmail,
            String customerName,
            String voucherNumber,
            byte[] pdfContent,
            byte[] xmlContent
    );
}
