package com.andeva.atelier.platform.invoicing.domain.model.valueobjects;

import com.andeva.atelier.platform.invoicing.domain.model.enums.CreditNoteReason;
import com.andeva.atelier.platform.invoicing.domain.model.ids.VoucherId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Audit and fiscal linkage to the original modified electronic voucher for credit notes.
 *
 * @author Joel Huamani Estefanero
 */
public record CreditNoteReference(
        VoucherId referenceVoucherId,
        VoucherSerie referenceSerie,
        VoucherNumber referenceNumber,
        CreditNoteReason reason,
        String reasonDescription
) implements Serializable {

    public CreditNoteReference {
        Objects.requireNonNull(referenceVoucherId, "Reference voucher ID cannot be null");
        Objects.requireNonNull(referenceSerie, "Reference series cannot be null");
        Objects.requireNonNull(referenceNumber, "Reference number cannot be null");
        Objects.requireNonNull(reason, "Credit note reason cannot be null");
        reasonDescription = reasonDescription != null ? reasonDescription.trim() : reason.getDescription();
    }

    public static CreditNoteReference of(
            VoucherId referenceVoucherId,
            VoucherSerie referenceSerie,
            VoucherNumber referenceNumber,
            CreditNoteReason reason,
            String reasonDescription
    ) {
        return new CreditNoteReference(referenceVoucherId, referenceSerie, referenceNumber, reason, reasonDescription);
    }
}
