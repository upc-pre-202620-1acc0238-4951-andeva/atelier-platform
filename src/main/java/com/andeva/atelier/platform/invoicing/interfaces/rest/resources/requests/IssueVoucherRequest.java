package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for issuing an official electronic voucher (Factura 01 or Boleta 03).
 *
 * @author Joel Huamani Estefanero
 */
public record IssueVoucherRequest(
        @NotNull(message = "{error.voucher.branch_id.required}")
        UUID branchId,

        @NotNull(message = "{error.voucher.customer_id.required}")
        UUID customerId,

        UUID workOrderId,

        @NotBlank(message = "{error.voucher.type.required}")
        @Pattern(regexp = "01|03", message = "{error.voucher.type.invalid}")
        String voucherType,

        @NotBlank(message = "{error.voucher.serie.required}")
        @Size(min = 4, max = 4, message = "{error.voucher.serie.length}")
        String serie,

        @NotNull(message = "{error.voucher.customer_info.required}")
        @Valid
        CustomerFiscalInfoRequest customerInfo,

        @NotBlank(message = "{error.voucher.currency.required}")
        @Pattern(regexp = "PEN|USD", message = "{error.voucher.currency.invalid}")
        String currency,

        @NotEmpty(message = "{error.voucher.lines.required}")
        @Valid
        List<VoucherLineRequest> lines
) implements Serializable {
}
