package com.andeva.atelier.platform.invoicing.interfaces.rest.resources.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.UUID;

/**
 * Request payload for configuring a new authorized fiscal series.
 *
 * @author Joel Huamani Estefanero
 */
public record ConfigureSeriesRequest(
        @NotNull(message = "{error.series.branch_id.required}")
        UUID branchId,

        @NotBlank(message = "{error.series.voucher_type.required}")
        String voucherType,

        @NotBlank(message = "{error.series.serie.required}")
        @Size(min = 4, max = 4, message = "{error.series.serie.length}")
        String serie,

        @Min(value = 0, message = "{error.series.initial_correlative.min}")
        int initialCorrelative
) implements Serializable {
}
