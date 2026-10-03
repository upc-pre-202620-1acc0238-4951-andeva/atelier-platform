package com.andeva.atelier.platform.billing.interfaces.rest.resources.responses;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * REST response resource providing financial and receipt details for a platform SaaS invoice.
 *
 * @author Joel Huamani Estefanero
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SaasInvoiceResource(
        UUID id,
        UUID subscriptionId,
        UUID tenantId,
        String stripeInvoiceId,
        BigDecimal amountPaid,
        String currency,
        String status,
        String invoicePdfUrl,
        String hostedInvoiceUrl,
        Instant paidAt,
        Instant createdAt
) {
}
