package com.andeva.atelier.platform.shared.domain.model.valueobjects;

/**
 * Formal catalog of accepted currencies in workshop and SaaS commercial operations.
 *
 * @author Joel Huamani Estefanero
 */
public enum Currency {
    PEN("Peruvian Sol", "S/."),
    USD("US Dollar", "$");

    private final String description;
    private final String symbol;

    Currency(String description, String symbol) {
        this.description = description;
        this.symbol = symbol;
    }

    public String description() {
        return description;
    }

    public String symbol() {
        return symbol;
    }
}
