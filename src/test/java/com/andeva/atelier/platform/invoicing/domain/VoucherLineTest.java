package com.andeva.atelier.platform.invoicing.domain;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.model.entities.VoucherLine;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherLineId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link VoucherLine} child entity.
 *
 * @author Joel Huamani Estefanero
 */
@DisplayName("VoucherLine Entity Unit Tests")
class VoucherLineTest {

    @Test
    @DisplayName("Should create VoucherLine and compute Peruvian tax breakdown with Banker's Rounding")
    void shouldCreateVoucherLineWithTaxBreakdown() {
        VoucherId voucherId = VoucherId.generate();
        UUID itemId = UUID.randomUUID();
        Quantity qty = Quantity.ofUnits(2);
        Money unitPrice = Money.soles(118.00); // Unit price with IGV

        VoucherLine line = VoucherLine.create(
                null,
                voucherId,
                itemId,
                VoucherItemType.PRODUCT,
                "Pastillas de freno delanteras Brembo",
                qty,
                unitPrice
        );

        assertThat(line.getId()).isNotNull();
        assertThat(line.getVoucherId()).isEqualTo(voucherId);
        assertThat(line.getItemId()).contains(itemId);
        assertThat(line.getItemType()).isEqualTo(VoucherItemType.PRODUCT);
        assertThat(line.getDescription()).isEqualTo("Pastillas de freno delanteras Brembo");
        assertThat(line.getQuantity()).isEqualTo(qty);

        // unitPrice = 118.00 -> unitValue = 118 / 1.18 = 100.00
        assertThat(line.getUnitValue().amount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(line.getUnitPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(118.00));

        // totalLine = 2 * 118.00 = 236.00
        assertThat(line.getTotalLine().amount()).isEqualByComparingTo(BigDecimal.valueOf(236.00));

        // igvAmount = 236.00 - (100.00 * 2) = 36.00
        assertThat(line.getIgvAmount().amount()).isEqualByComparingTo(BigDecimal.valueOf(36.00));
    }

    @Test
    @DisplayName("Should reject line with zero quantity")
    void shouldRejectZeroQuantity() {
        VoucherId voucherId = VoucherId.generate();
        Money price = Money.soles(50.00);

        assertThatThrownBy(() -> VoucherLine.create(
                null, voucherId, null, VoucherItemType.SERVICE, "Mano de obra", Quantity.ofUnits(0), price
        )).isInstanceOf(InvalidVoucherAmountException.class);
    }

    @Test
    @DisplayName("Should reject line with negative price")
    void shouldRejectNegativePrice() {
        VoucherId voucherId = VoucherId.generate();
        Money negativePrice = Money.soles(-50.00);

        assertThatThrownBy(() -> VoucherLine.create(
                null, voucherId, null, VoucherItemType.SERVICE, "Mano de obra", Quantity.ofUnits(1), negativePrice
        )).isInstanceOf(InvalidVoucherAmountException.class);
    }

    @Test
    @DisplayName("Should reject line with blank description")
    void shouldRejectBlankDescription() {
        VoucherId voucherId = VoucherId.generate();
        Money price = Money.soles(50.00);

        assertThatThrownBy(() -> VoucherLine.create(
                null, voucherId, null, VoucherItemType.SERVICE, "   ", Quantity.ofUnits(1), price
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
