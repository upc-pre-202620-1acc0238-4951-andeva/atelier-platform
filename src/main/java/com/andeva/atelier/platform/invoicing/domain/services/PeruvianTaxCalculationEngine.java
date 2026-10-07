package com.andeva.atelier.platform.invoicing.domain.services;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherLineId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain service applying Peruvian tax segregation formulas between taxable base
 * and General Sales Tax (IGV 18%) using legal Banker's Rounding (Half-Even).
 *
 * @author Joel Huamani Estefanero
 */
@Service
public class PeruvianTaxCalculationEngine {

    public static final BigDecimal IGV_FACTOR = BigDecimal.valueOf(1.18);
    public static final BigDecimal IGV_RATE = BigDecimal.valueOf(0.18);

    public TaxCalculation calculateFromGrossTotal(Money grossTotal) {
        Objects.requireNonNull(grossTotal, "Gross total cannot be null");

        BigDecimal gross = grossTotal.amount();
        BigDecimal subtotal = gross.divide(IGV_FACTOR, 2, RoundingMode.HALF_EVEN);
        BigDecimal igv = gross.subtract(subtotal);

        return TaxCalculation.of(
                Money.of(subtotal, grossTotal.currency()),
                Money.of(igv, grossTotal.currency()),
                grossTotal,
                IGV_RATE
        );
    }

    public TaxCalculation calculateFromLines(List<VoucherLine> lines, Currency currency) {
        Objects.requireNonNull(lines, "Lines cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        if (lines.isEmpty()) {
            return TaxCalculation.of(Money.of(BigDecimal.ZERO, currency), Money.of(BigDecimal.ZERO, currency), Money.of(BigDecimal.ZERO, currency));
        }

        BigDecimal totalSum = lines.stream()
                .map(l -> l.getTotalLine().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal igvSum = lines.stream()
                .map(l -> l.getIgvAmount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotalSum = totalSum.subtract(igvSum);

        return TaxCalculation.of(
                Money.of(subtotalSum, currency),
                Money.of(igvSum, currency),
                Money.of(totalSum, currency),
                IGV_RATE
        );
    }

    public Money extractUnitValue(Money unitPriceWithIgv) {
        Objects.requireNonNull(unitPriceWithIgv, "Unit price cannot be null");
        BigDecimal unitValue = unitPriceWithIgv.amount().divide(IGV_FACTOR, 4, RoundingMode.HALF_EVEN)
                .setScale(2, RoundingMode.HALF_EVEN);
        return Money.of(unitValue, unitPriceWithIgv.currency());
    }

    public VoucherLine calculateLine(
            VoucherLineId id,
            VoucherId voucherId,
            Optional<UUID> itemId,
            VoucherItemType itemType,
            String description,
            Quantity quantity,
            Money unitPriceWithIgv
    ) {
        return VoucherLine.create(
                id,
                voucherId,
                itemId.orElse(null),
                itemType,
                description,
                quantity,
                unitPriceWithIgv
        );
    }

    /**
     * Converts a monetary amount to official Spanish words format required for legal vouchers
     * (e.g. "SON: CIENTO CINCUENTA Y 50/100 SOLES").
     */
    public String convertAmountToWords(Money amount, Currency currency) {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");

        BigDecimal val = amount.amount().setScale(2, RoundingMode.HALF_EVEN);
        long integerPart = val.longValue();
        int cents = val.remainder(BigDecimal.ONE).movePointRight(2).intValue();

        String integerWords = convertIntegerToSpanishWords(integerPart);
        String currencyUnit = currency == Currency.PEN ? "SOLES" : "DÓLARES AMERICANOS";

        return String.format("SON: %s CON %02d/100 %s", integerWords, cents, currencyUnit);
    }

    private String convertIntegerToSpanishWords(long n) {
        if (n == 0) return "CERO";
        if (n == 100) return "CIEN";

        String[] units = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE"};
        String[] teens = {"DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISÉIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE"};
        String[] tens = {"", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
        String[] hundreds = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};

        if (n < 10) return units[(int) n];
        if (n < 20) return teens[(int) n - 10];
        if (n < 100) {
            int ten = (int) (n / 10);
            int unit = (int) (n % 10);
            if (unit == 0) return tens[ten];
            if (ten == 2) return "VEINTI" + units[unit];
            return tens[ten] + " Y " + units[unit];
        }
        if (n < 1000) {
            int hundred = (int) (n / 100);
            long rest = n % 100;
            if (rest == 0) return hundreds[hundred];
            return hundreds[hundred] + " " + convertIntegerToSpanishWords(rest);
        }
        if (n < 1_000_000) {
            long thousands = n / 1000;
            long rest = n % 1000;
            String prefix = thousands == 1 ? "MIL" : convertIntegerToSpanishWords(thousands) + " MIL";
            if (rest == 0) return prefix;
            return prefix + " " + convertIntegerToSpanishWords(rest);
        }
        if (n < 1_000_000_000L) {
            long millions = n / 1_000_000;
            long rest = n % 1_000_000;
            String prefix = millions == 1 ? "UN MILLÓN" : convertIntegerToSpanishWords(millions) + " MILLONES";
            if (rest == 0) return prefix;
            return prefix + " " + convertIntegerToSpanishWords(rest);
        }

        return String.valueOf(n);
    }
}
