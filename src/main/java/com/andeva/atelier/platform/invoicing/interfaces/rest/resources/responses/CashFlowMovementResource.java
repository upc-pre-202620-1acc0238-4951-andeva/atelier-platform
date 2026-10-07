package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Presentation resource detailing a single financial transaction movement in the cash flow ledger.
 *
 * @author Joel Huamani Estefanero
 */
public record CashFlowMovementResource(
        UUID transactionId,
        Instant movementDate,
        String type,
        String category,
        String concept,
        String referenceNumber,
        BigDecimal amount,
        BigDecimal runningBalance
) implements Serializable {
}
