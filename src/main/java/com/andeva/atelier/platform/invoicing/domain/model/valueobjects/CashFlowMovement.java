package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable transaction movement entry within the workshop cash flow ledger.
 *
 * @author Joel Huamani Estefanero
 */
public record CashFlowMovement(
        UUID transactionId,
        Instant movementDate,
        String type,
        String category,
        String concept,
        String referenceNumber,
        BigDecimal amount,
        BigDecimal runningBalance
) implements Serializable {

    public CashFlowMovement {
        Objects.requireNonNull(transactionId, "Transaction ID cannot be null");
        Objects.requireNonNull(movementDate, "Movement date cannot be null");
        Objects.requireNonNull(type, "Movement type cannot be null");
        Objects.requireNonNull(category, "Movement category cannot be null");
        Objects.requireNonNull(concept, "Concept cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(runningBalance, "Running balance cannot be null");
    }

    public static CashFlowMovement of(
            UUID transactionId,
            Instant movementDate,
            String type,
            String category,
            String concept,
            String referenceNumber,
            BigDecimal amount,
            BigDecimal runningBalance
    ) {
        return new CashFlowMovement(transactionId, movementDate, type, category, concept, referenceNumber, amount, runningBalance);
    }
}
