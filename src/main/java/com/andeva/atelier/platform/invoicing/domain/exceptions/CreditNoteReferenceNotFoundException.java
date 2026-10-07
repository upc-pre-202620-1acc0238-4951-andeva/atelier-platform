package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

/**
 * Thrown when a credit note references an original issuer voucher that cannot be found or is invalid.
 *
 * @author Joel Huamani Estefanero
 */
public class CreditNoteReferenceNotFoundException extends InvoicingDomainException {

    public static final String ERROR_CODE = "ERR_CREDIT_NOTE_REF_NOT_FOUND";

    public CreditNoteReferenceNotFoundException(VoucherId referenceVoucherId) {
        super(ERROR_CODE, "Original electronic voucher '" + referenceVoucherId.value() + "' referenced by the credit note was not found.");
    }

    public CreditNoteReferenceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
