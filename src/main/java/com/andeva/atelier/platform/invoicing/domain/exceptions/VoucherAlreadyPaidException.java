package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

/**
 * Thrown when attempting to register a financial payment exceeding the outstanding balance
 * or against a fully amortized electronic voucher.
 *
 * @author Joel Huamani Estefanero
 */
public class VoucherAlreadyPaidException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_VOUCHER_ALREADY_PAID";

    public VoucherAlreadyPaidException(VoucherId voucherId) {
        super(ERROR_CODE, "Electronic voucher '" + voucherId.value() + "' has already been fully paid or the payment amount exceeds the pending balance.");
    }

    public VoucherAlreadyPaidException(String message) {
        super(ERROR_CODE, message);
    }
}
