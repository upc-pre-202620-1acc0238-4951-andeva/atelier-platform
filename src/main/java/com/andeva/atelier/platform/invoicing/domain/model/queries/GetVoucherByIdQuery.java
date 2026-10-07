package com.andeva.atelier.platform.invoicing.domain.model.queries;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Query requesting an electronic voucher by its unique identifier.
 *
 * @author Joel Huamani Estefanero
 */
public record GetVoucherByIdQuery(VoucherId voucherId) implements Serializable {

    public GetVoucherByIdQuery {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
    }
}
