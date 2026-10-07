package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Consolidated tax computation value object representing taxable base (subtotal),
 * 18% general sales tax (IGV), and gross total amount.
 * Enforces the invariant: subtotal + igvAmount == totalAmount.
 *
 * @author Joel Huamani Estefanero
 */
public record TaxCalculation(
        Money subtotal,
        Money igvAmount,
        Money totalAmount,
        BigDecimal igvRate
) implements Serializable {

    public static final BigDecimal DEFAULT_IGV_RATE = BigDecimal.valueOf(0.18);

    public TaxCalculation {
        Objects.requireNonNull(subtotal, "Subtotal amount cannot be null");
        Objects.requireNonNull(igvAmount, "IGV amount cannot be null");
        Objects.requireNonNull(totalAmount, "Total amount cannot be null");
        Objects.requireNonNull(igvRate, "IGV rate cannot be null");

        if (subtotal.amount().compareTo(BigDecimal.ZERO) < 0 ||
                igvAmount.amount().compareTo(BigDecimal.ZERO) < 0 ||
                totalAmount.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidVoucherAmountException("Voucher amounts cannot be negative");
        }

        BigDecimal calculatedTotal = subtotal.amount().add(igvAmount.amount());
        if (calculatedTotal.compareTo(totalAmount.amount()) != 0) {
            throw new InvalidVoucherAmountException(
                    "Tax calculation arithmetic inconsistency: subtotal (" + subtotal.amount() +
                            ") + IGV (" + igvAmount.amount() + ") does not equal total (" + totalAmount.amount() + ")"
            );
        }
    }

    public static TaxCalculation of(Money subtotal, Money igvAmount, Money totalAmount) {
        return new TaxCalculation(subtotal, igvAmount, totalAmount, DEFAULT_IGV_RATE);
    }

    public static TaxCalculation of(Money subtotal, Money igvAmount, Money totalAmount, BigDecimal igvRate) {
        return new TaxCalculation(subtotal, igvAmount, totalAmount, igvRate);
    }
}
