package com.andeva.atelier.platform.invoicing.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when an electronic voucher is rejected by SUNAT
 * or the authorized PSE due to fiscal or normative inconsistencies.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherRejectedBySunatIntegrationEvent(
        UUID voucherId,
        UUID tenantId,
        String errorCode,
        String errorMessage,
        Instant occurredOn
) implements Serializable {}
