package com.andeva.atelier.platform.invoicing.interfaces.events;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when an electronic voucher is voided or cancelled
 * before SUNAT via communication of baja.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherVoidedIntegrationEvent(
        UUID voucherId,
        UUID tenantId,
        String reason,
        Instant occurredOn
) implements Serializable {}
