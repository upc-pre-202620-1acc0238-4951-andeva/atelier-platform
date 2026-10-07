package com.andeva.atelier.platform.invoicing.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when an electronic voucher receives formal
 * SUNAT acceptance verification (CDR).
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherAcceptedBySunatIntegrationEvent(
        UUID voucherId,
        UUID tenantId,
        String digitalSignatureHash,
        String pdfUrl,
        String xmlUrl,
        String cdrUrl,
        Instant occurredOn
) implements Serializable {}
