package com.andeva.atelier.platform.invoicing.domain.exceptions;

/**
 * Thrown when an arithmetic discrepancy exists between itemized line items and header totals,
 * or when monetary amounts are null, zero, or negative.
 *
 * @author Joel Huamani Estefanero
 */
public class InvalidVoucherAmountException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_INVALID_VOUCHER_AMOUNT";

    public InvalidVoucherAmountException(String message) {
        super(ERROR_CODE, message);
    }
}
