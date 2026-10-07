package com.andeva.atelier.platform.invoicing.domain.model.queries;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Query requesting all payment transactions for an electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public record GetVoucherPaymentsQuery(VoucherId voucherId) implements Serializable {

    public GetVoucherPaymentsQuery {
        Objects.requireNonNull(voucherId, "Voucher ID cannot be null");
    }
}
