package com.andeva.atelier.platform.shared.domain.exceptions;

/**
 * Thrown when attempting arithmetic or comparison operations between monetary amounts of different currencies.
 *
 * @author Joel Huamani Estefanero
 */
public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException(String sourceCurrency, String targetCurrency) {
        super("CURRENCY_MISMATCH", String.format("Currency mismatch: cannot operate %s with %s", sourceCurrency, targetCurrency));
    }
}
