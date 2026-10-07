package com.andeva.atelier.platform.invoicing.interfaces.acl.dto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Itemized billable line extracted from an MRO work order task or spare part.
 *
 * @author Joel Huamani Estefanero
 */
public record WorkOrderItemBillingDto(
        UUID itemId,
        String itemType,
        String description,
        BigDecimal quantity,
        BigDecimal unitPriceWithIgv
) {
    public WorkOrderItemBillingDto {
        Objects.requireNonNull(itemType, "ItemType cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");
        Objects.requireNonNull(quantity, "Quantity cannot be null");
        Objects.requireNonNull(unitPriceWithIgv, "UnitPriceWithIgv cannot be null");
    }
}
