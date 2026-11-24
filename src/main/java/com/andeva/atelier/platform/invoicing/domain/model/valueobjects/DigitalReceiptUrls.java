package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import java.io.Serializable;

/**
 * Publicly accessible and secure URLs for the generated digital artifacts
 * (PDF representation, signed XML UBL 2.1 document, and SUNAT CDR receipt).
 *
 * @author Joel Huamani Estefanero
 */
public record DigitalReceiptUrls(
        String pdfUrl,
        String xmlUrl,
        String cdrUrl,
        String qrCodeData
) implements Serializable {

    public DigitalReceiptUrls(String pdfUrl, String xmlUrl, String cdrUrl) {
        this(pdfUrl, xmlUrl, cdrUrl, null);
    }

    public static DigitalReceiptUrls empty() {
        return new DigitalReceiptUrls(null, null, null, null);
    }

    public static DigitalReceiptUrls of(String pdfUrl, String xmlUrl, String cdrUrl) {
        return new DigitalReceiptUrls(pdfUrl, xmlUrl, cdrUrl, null);
    }

    public static DigitalReceiptUrls of(String pdfUrl, String xmlUrl, String cdrUrl, String qrCodeData) {
        return new DigitalReceiptUrls(pdfUrl, xmlUrl, cdrUrl, qrCodeData);
    }
}
