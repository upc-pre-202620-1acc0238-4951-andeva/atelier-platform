package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Compact REST response resource optimized for tabular invoice histories and receipts listings.
 *
 * @author Joel Huamani Estefanero
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaasInvoiceSummaryResource(
        UUID id,
        String stripeInvoiceId,
        BigDecimal amountPaid,
        String currency,
        String status,
        Instant paidAt
) {
}
