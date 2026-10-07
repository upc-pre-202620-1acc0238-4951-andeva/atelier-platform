package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Presentation resource detailing a payment or cash collection against an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherPaymentResource(
        UUID id,
        UUID voucherId,
        BigDecimal amount,
        String currency,
        String paymentMethod,
        String transactionReference,
        String status,
        Instant paidAt
) implements Serializable {
}
