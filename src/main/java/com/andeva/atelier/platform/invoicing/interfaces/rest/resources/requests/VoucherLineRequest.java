package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request payload representing an itemized invoice line item for parts or labor.
 *
 * @author Joel Huamani Estefanero
 */
public record VoucherLineRequest(
        UUID itemId,

        @NotBlank(message = "{error.line.item_type.required}")
        @Pattern(regexp = "PRODUCT|SERVICE", message = "{error.line.item_type.invalid}")
        String itemType,

        @NotBlank(message = "{error.line.description.required}")
        @Size(max = 200, message = "{error.line.description.length}")
        String description,

        @NotNull(message = "{error.line.quantity.required}")
        @DecimalMin(value = "0.01", message = "{error.line.quantity.min}")
        BigDecimal quantity,

        @NotNull(message = "{error.line.price.required}")
        @DecimalMin(value = "0.01", message = "{error.line.price.min}")
        BigDecimal unitPriceWithIgv
) implements Serializable {
}
