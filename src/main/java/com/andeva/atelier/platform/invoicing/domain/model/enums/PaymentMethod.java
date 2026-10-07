package com.andeva.atelier.platform.invoicing.domain.model.enums;

import java.util.Objects;

/**
 * Commercial payment methods authorized for workshop settlements.
 *
 * @author Joel Huamani Estefanero
 */
public enum PaymentMethod {
    CASH("Efectivo", false),
    CREDIT_CARD("Tarjeta de Crédito", true),
    DEBIT_CARD("Tarjeta de Débito", true),
    BANK_TRANSFER("Transferencia Bancaria", true),
    DIGITAL_WALLET_YAPE("Billetera Digital Yape", true),
    DIGITAL_WALLET_PLIN("Billetera Digital Plin", true);

    private final String displayName;
    private final boolean requiresTransactionReference;

    PaymentMethod(String displayName, boolean requiresTransactionReference) {
        this.displayName = Objects.requireNonNull(displayName, "Display name cannot be null");
        this.requiresTransactionReference = requiresTransactionReference;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean requiresTransactionReference() {
        return requiresTransactionReference;
    }
}
