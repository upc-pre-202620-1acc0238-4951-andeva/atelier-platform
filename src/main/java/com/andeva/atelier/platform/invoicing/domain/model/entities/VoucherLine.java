package com.andeva.atelier.platform.invoicing.domain.model.entities;

import com.andeva.atelier.platform.invoicing.domain.exceptions.InvalidVoucherAmountException;
import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherLineId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Child entity representing an itemized billable line in an electronic voucher.
 * Implements UBL 2.1 formulas under Banker's Rounding (Half-Even):
 * - unitValue = unitPrice / 1.18
 * - totalLine = quantity * unitPrice
 * - igvAmount = totalLine - (unitValue * quantity)
 *
 * @author Joel Huamani Estefanero
 */
public class VoucherLine implements Serializable {

    private static final BigDecimal IGV_FACTOR = BigDecimal.valueOf(1.18);

    private final VoucherLineId id;
    private final VoucherId voucherId;
    private final UUID itemId;
    private final VoucherItemType itemType;
    private final String description;
    private final Quantity quantity;
    private final Money unitValue;
    private final Money unitPrice;
    private final Money igvAmount;
    private final Money totalLine;

    public VoucherLine(
            VoucherLineId id,
            VoucherId voucherId,
            UUID itemId,
            VoucherItemType itemType,
            String description,
            Quantity quantity,
            Money unitValue,
            Money unitPrice,
            Money igvAmount,
            Money totalLine
    ) {
        this.id = Objects.requireNonNull(id, "Voucher line ID cannot be null");
        this.voucherId = Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
        this.itemId = itemId;
        this.itemType = Objects.requireNonNull(itemType, "Item type cannot be null");
        this.description = Objects.requireNonNull(description, "Description cannot be null").trim();
        this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
        this.unitValue = Objects.requireNonNull(unitValue, "Unit value cannot be null");
        this.unitPrice = Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        this.igvAmount = Objects.requireNonNull(igvAmount, "IGV amount cannot be null");
        this.totalLine = Objects.requireNonNull(totalLine, "Total line amount cannot be null");

        if (description.isBlank()) {
            throw new IllegalArgumentException("Line description cannot be blank");
        }
        if (quantity.value().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidVoucherAmountException("Line quantity must be greater than zero");
        }
        if (unitPrice.amount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidVoucherAmountException("Line unit price cannot be negative");
        }
    }

    public static VoucherLine create(
            VoucherLineId id,
            VoucherId voucherId,
            UUID itemId,
            VoucherItemType itemType,
            String description,
            Quantity quantity,
            Money unitPrice
    ) {
        Objects.requireNonNull(unitPrice, "Unit price cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");

        // totalLine = quantity * unitPrice
        BigDecimal totalRaw = quantity.value().multiply(unitPrice.amount()).setScale(2, RoundingMode.HALF_EVEN);
        Money totalLine = Money.of(totalRaw, unitPrice.currency());

        // unitValue = unitPrice / 1.18 (4 decimals scale for UBL 2.1 calculation, scaled to 2 for line value)
        BigDecimal unitValueRaw = unitPrice.amount().divide(IGV_FACTOR, 4, RoundingMode.HALF_EVEN)
                .setScale(2, RoundingMode.HALF_EVEN);
        Money unitValue = Money.of(unitValueRaw, unitPrice.currency());

        // subtotalLine = unitValue * quantity
        BigDecimal subtotalLineRaw = unitValueRaw.multiply(quantity.value()).setScale(2, RoundingMode.HALF_EVEN);

        // igvAmount = totalLine - subtotalLine
        BigDecimal igvRaw = totalRaw.subtract(subtotalLineRaw).setScale(2, RoundingMode.HALF_EVEN);
        Money igvAmount = Money.of(igvRaw, unitPrice.currency());

        return new VoucherLine(
                id != null ? id : VoucherLineId.generate(),
                voucherId,
                itemId,
                itemType,
                description,
                quantity,
                unitValue,
                unitPrice,
                igvAmount,
                totalLine
        );
    }

    public VoucherLineId getId() {
        return id;
    }

    public VoucherId getVoucherId() {
        return voucherId;
    }

    public Optional<UUID> getItemId() {
        return Optional.ofNullable(itemId);
    }

    public VoucherItemType getItemType() {
        return itemType;
    }

    public String getDescription() {
        return description;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitValue() {
        return unitValue;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getIgvAmount() {
        return igvAmount;
    }

    public Money getTotalLine() {
        return totalLine;
    }
}
