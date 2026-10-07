package com.andeva.atelier.platform.invoicing.domain.model.commands;

import com.andeva.atelier.platform.invoicing.domain.model.enums.VoucherItemType;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Quantity;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Command payload representing an itemized detail line to be added to an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherLineCommandDto(
        Optional<UUID> itemId,
        VoucherItemType itemType,
        String description,
        Quantity quantity,
        Money unitPriceWithIgv
) implements Serializable {

    public VoucherLineCommandDto {
        Objects.requireNonNull(itemType, "Item type cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPriceWithIgv, "Unit price cannot be null");
        description = description.trim();
        if (description.isBlank()) {
            throw new IllegalArgumentException("Line description cannot be blank");
        }
    }

    public static VoucherLineCommandDto of(
            UUID itemId,
            VoucherItemType itemType,
            String description,
            Quantity quantity,
            Money unitPriceWithIgv
    ) {
        return new VoucherLineCommandDto(Optional.ofNullable(itemId), itemType, description, quantity, unitPriceWithIgv);
    }
}
