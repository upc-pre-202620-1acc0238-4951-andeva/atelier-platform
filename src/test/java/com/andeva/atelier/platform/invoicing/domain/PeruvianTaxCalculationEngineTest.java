package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherLineId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.TaxCalculation;
import com.andeva.atelier.platform.invoicing.domain.services.PeruvianTaxCalculationEngine;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for PeruvianTaxCalculationEngine domain service.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("PeruvianTaxCalculationEngine Domain Service Tests")
class PeruvianTaxCalculationEngineTest {

    private PeruvianTaxCalculationEngine taxEngine;

    @BeforeEach
    void setUp() {
        taxEngine = new PeruvianTaxCalculationEngine();
    }

    @Test
    @DisplayName("Should correctly calculate tax segregation from gross total (118.00 PEN -> 100.00 subtotal, 18.00 IGV)")
    void shouldCalculateFromGrossTotal() {
        Money gross = Money.of(new BigDecimal("118.00"), Currency.PEN);
        TaxCalculation calc = taxEngine.calculateFromGrossTotal(gross);

        assertThat(calc.subtotal().amount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(calc.igvAmount().amount()).isEqualByComparingTo(new BigDecimal("18.00"));
        assertThat(calc.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("118.00"));
        assertThat(calc.igvRate()).isEqualByComparingTo(new BigDecimal("0.18"));
    }

    @Test
    @DisplayName("Should extract unit value from gross price with Banker's Rounding Half-Even")
    void shouldExtractUnitValue() {
        Money unitPrice = Money.of(new BigDecimal("118.00"), Currency.PEN);
        Money unitValue = taxEngine.extractUnitValue(unitPrice);

        assertThat(unitValue.amount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Should calculate line and aggregate multiple lines correctly")
    void shouldCalculateFromLines() {
        VoucherId voucherId = VoucherId.generate();
        VoucherLine line1 = taxEngine.calculateLine(
                VoucherLineId.generate(),
                voucherId,
                Optional.of(UUID.randomUUID()),
                VoucherItemType.PRODUCT,
                "Oil Filter",
                Quantity.ofUnits(2),
                Money.of(new BigDecimal("59.00"), Currency.PEN) // total 118.00
        );

        VoucherLine line2 = taxEngine.calculateLine(
                VoucherLineId.generate(),
                voucherId,
                Optional.empty(),
                VoucherItemType.SERVICE,
                "Diagnostic Scan",
                Quantity.ofUnits(1),
                Money.of(new BigDecimal("236.00"), Currency.PEN) // total 236.00 -> subtotal 200.00, igv 36.00
        );

        TaxCalculation aggregateCalc = taxEngine.calculateFromLines(List.of(line1, line2), Currency.PEN);

        assertThat(aggregateCalc.totalAmount().amount()).isEqualByComparingTo(new BigDecimal("354.00"));
        assertThat(aggregateCalc.subtotal().amount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(aggregateCalc.igvAmount().amount()).isEqualByComparingTo(new BigDecimal("54.00"));
    }

    @Test
    @DisplayName("Should return zero amounts when lines list is empty")
    void shouldReturnZeroForEmptyLines() {
        TaxCalculation calc = taxEngine.calculateFromLines(List.of(), Currency.PEN);

        assertThat(calc.totalAmount().amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(calc.subtotal().amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(calc.igvAmount().amount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should convert amount to Spanish words in PEN (Soles)")
    void shouldConvertAmountToWordsInPEN() {
        Money amount = Money.of(new BigDecimal("150.50"), Currency.PEN);
        String words = taxEngine.convertAmountToWords(amount, Currency.PEN);

        assertThat(words).isEqualTo("SON: CIENTO CINCUENTA CON 50/100 SOLES");
    }

    @Test
    @DisplayName("Should convert amount to Spanish words in USD (Dólares Americanos)")
    void shouldConvertAmountToWordsInUSD() {
        Money amount = Money.of(new BigDecimal("1200.00"), Currency.USD);
        String words = taxEngine.convertAmountToWords(amount, Currency.USD);

        assertThat(words).isEqualTo("SON: MIL DOSCIENTOS CON 00/100 DÓLARES AMERICANOS");
    }

    @Test
    @DisplayName("Should reject null arguments")
    void shouldRejectNullArguments() {
        assertThatThrownBy(() -> taxEngine.calculateFromGrossTotal(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> taxEngine.calculateFromLines(null, Currency.PEN))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> taxEngine.extractUnitValue(null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> taxEngine.convertAmountToWords(null, Currency.PEN))
                .isInstanceOf(NullPointerException.class);
    }
}
