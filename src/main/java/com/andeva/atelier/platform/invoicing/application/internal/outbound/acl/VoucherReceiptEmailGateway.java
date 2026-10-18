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
     * Sends transactional email attaching the legal receipt PDF and UBL 2.1 XML hosted by the PSE.
     *
     * @param pdfUrl public URL of the PDF representation (may be null)
     * @param xmlUrl public URL of the signed XML (may be null)
     */
    void sendVoucherReceiptEmail(
            UUID voucherId,
            String recipientEmail,
            String customerName,
            String voucherNumber,
            String pdfUrl,
            String xmlUrl
    );
}
