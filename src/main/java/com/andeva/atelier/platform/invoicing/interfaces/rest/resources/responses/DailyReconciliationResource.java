package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Presentation resource providing daily cash reconciliation summary by payment method for a physical branch.
 *
 * @author Joel Huamani Estefanero
 */
public record DailyReconciliationResource(
        UUID branchId,
        LocalDate date,
        BigDecimal totalCollected,
        String currency,
        int paymentCount,
        Map<String, BigDecimal> breakdownByMethod,
        List<VoucherPaymentResource> payments
) implements Serializable {
}
