package com.andeva.atelier.platform.shared.domain.model.valueobjects;

import com.andeva.atelier.platform.shared.domain.exceptions.CurrencyMismatchException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable financial Value Object representing monetary magnitudes with
 * exact 2-decimal scale under Banker's Rounding (Half-Even).
 *
 * @author Joel Huamani Estefanero
 */
public record Money(BigDecimal amount, Currency currency) {

    public static final Money ZERO_PEN = new Money(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), Currency.PEN);
    public static final Money ZERO_USD = new Money(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), Currency.USD);

    public Money {
        Objects.requireNonNull(amount, "Monetary amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money soles(BigDecimal amount) {
        return new Money(amount, Currency.PEN);
    }

    public static Money soles(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.PEN);
    }

    public static Money dollars(BigDecimal amount) {
        return new Money(amount, Currency.USD);
    }

    public static Money dollars(double amount) {
        return new Money(BigDecimal.valueOf(amount), Currency.USD);
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        Objects.requireNonNull(factor, "Multiplication factor cannot be null");
        return new Money(this.amount.multiply(factor), this.currency);
    }

    public Money multiply(double factor) {
        return multiply(BigDecimal.valueOf(factor));
    }

    public Money divide(BigDecimal divisor) {
        Objects.requireNonNull(divisor, "Divisor cannot be null");
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new ArithmeticException("Cannot divide a monetary magnitude by zero");
        }
        return new Money(this.amount.divide(divisor, 2, RoundingMode.HALF_EVEN), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    private void validateSameCurrency(Money other) {
        Objects.requireNonNull(other, "Monetary amount to compare cannot be null");
        if (this.currency != other.currency) {
            throw new CurrencyMismatchException(this.currency.name(), other.currency.name());
        }
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency.symbol(), amount.toPlainString());
    }
}
