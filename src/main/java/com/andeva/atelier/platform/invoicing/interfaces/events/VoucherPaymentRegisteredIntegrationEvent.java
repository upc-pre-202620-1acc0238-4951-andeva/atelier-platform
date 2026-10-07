package com.andeva.atelier.platform.invoicing.interfaces.events;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Integration Event published when a cash or digital payment is recorded
 * against an electronic voucher. Notifies MRO to authorize vehicle gate pass.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherPaymentRegisteredIntegrationEvent(
        UUID paymentId,
        UUID voucherId,
        UUID tenantId,
        UUID branchId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        boolean isFullyPaid,
        Instant occurredOn
) implements Serializable {}
