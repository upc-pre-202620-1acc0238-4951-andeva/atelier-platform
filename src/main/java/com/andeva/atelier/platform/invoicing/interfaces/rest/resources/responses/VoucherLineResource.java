package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.responses;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Presentation resource detailing an itemized line item within an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherLineResource(
        UUID id,
        UUID itemId,
        String itemType,
        String description,
        BigDecimal quantity,
        BigDecimal unitValue,
        BigDecimal unitPrice,
        BigDecimal igvAmount,
        BigDecimal totalLine
) implements Serializable {
}
