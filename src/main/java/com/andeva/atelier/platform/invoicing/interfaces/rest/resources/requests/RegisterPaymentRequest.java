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
 * Request payload for registering a payment or customer cash collection against an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record RegisterPaymentRequest(
        @NotNull(message = "{error.payment.voucher_id.required}")
        UUID voucherId,

        @NotNull(message = "{error.payment.branch_id.required}")
        UUID branchId,

        @NotNull(message = "{error.payment.amount.required}")
        @DecimalMin(value = "0.01", message = "{error.payment.amount.min}")
        BigDecimal amount,

        @NotBlank(message = "{error.payment.currency.required}")
        @Pattern(regexp = "PEN|USD", message = "{error.payment.currency.invalid}")
        String currency,

        @NotBlank(message = "{error.payment.method.required}")
        String paymentMethod,

        @Size(max = 100, message = "{error.payment.reference.length}")
        String transactionReference
) implements Serializable {
}
