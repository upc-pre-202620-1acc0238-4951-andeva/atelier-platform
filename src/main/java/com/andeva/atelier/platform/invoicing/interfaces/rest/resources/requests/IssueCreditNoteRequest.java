package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Request payload for issuing an electronic credit note (Nota de Crédito 07)
 * referencing an accepted electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record IssueCreditNoteRequest(
        @NotNull(message = "{error.credit_note.branch_id.required}")
        UUID branchId,

        @NotNull(message = "{error.credit_note.customer_id.required}")
        UUID customerId,

        @NotNull(message = "{error.credit_note.reference_voucher_id.required}")
        UUID referenceVoucherId,

        @NotBlank(message = "{error.credit_note.serie.required}")
        @Size(min = 4, max = 4, message = "{error.credit_note.serie.length}")
        String serie,

        @NotBlank(message = "{error.credit_note.reason.required}")
        String reason,

        @NotBlank(message = "{error.credit_note.reason_description.required}")
        @Size(max = 250, message = "{error.credit_note.reason_description.length}")
        String reasonDescription,

        @NotEmpty(message = "{error.credit_note.lines.required}")
        @Valid
        List<VoucherLineRequest> lines
) implements Serializable {
}
