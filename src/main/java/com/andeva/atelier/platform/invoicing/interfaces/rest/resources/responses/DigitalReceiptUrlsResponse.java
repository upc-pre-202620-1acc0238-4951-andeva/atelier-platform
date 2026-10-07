package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;

/**
 * Presentation resource providing secure download hyperlinks for legal receipt artifacts.
 *
 * @author Joel Huamani Estefanero
 */
public record DigitalReceiptUrlsResponse(
        String pdfUrl,
        String xmlUrl,
        String cdrUrl
) implements Serializable {
}
