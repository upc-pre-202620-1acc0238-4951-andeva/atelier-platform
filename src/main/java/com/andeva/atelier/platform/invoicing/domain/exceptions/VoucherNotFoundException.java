package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherNumber;
import com.andeva.atelier.platform.invoicing.domain.model.valueobjects.VoucherSerie;

/**
 * Thrown when an electronic voucher cannot be located in the repository.
 *
 * @author Joel Huamani Estefanero
 */
public class VoucherNotFoundException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_VOUCHER_NOT_FOUND";

    public VoucherNotFoundException(VoucherId id) {
        super(ERROR_CODE, "Electronic voucher with ID '" + id.value() + "' was not found.");
    }

    public VoucherNotFoundException(VoucherSerie serie, VoucherNumber number) {
        super(ERROR_CODE, "Electronic voucher with series number '" + serie.value() + "-" + number.format() + "' was not found.");
    }

    public VoucherNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
