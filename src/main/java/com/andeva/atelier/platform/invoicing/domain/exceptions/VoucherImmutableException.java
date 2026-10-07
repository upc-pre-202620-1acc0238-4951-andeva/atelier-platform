package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

/**
 * Thrown when an illegal attempt is made to mutate, recalculate, or alter an electronic voucher
 * that has already achieved fiscal acceptance by SUNAT.
 *
 * @author Joel Huamani Estefanero
 */
public class VoucherImmutableException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_VOUCHER_IMMUTABLE";

    public VoucherImmutableException(VoucherId voucherId) {
        super(ERROR_CODE, "Electronic voucher '" + voucherId.value() + "' has been accepted by SUNAT and is legally immutable. Any correction must be executed via Credit Note.");
    }

    public VoucherImmutableException(String message) {
        super(ERROR_CODE, message);
    }
}
