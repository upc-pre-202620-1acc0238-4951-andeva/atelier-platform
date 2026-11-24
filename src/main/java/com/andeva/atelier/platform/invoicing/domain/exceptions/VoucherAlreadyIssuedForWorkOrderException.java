package com.andeva.atelier.platform.invoicing.domain.exceptions;

import com.andeva.atelier.platform.shared.domain.model.valueobjects.WorkOrderId;

/**
 * Thrown when attempting to issue a duplicate active sales voucher (Factura or Boleta)
 * for a work order that already has an active voucher in ISSUED or ACCEPTED_SUNAT status.
 */
public class VoucherAlreadyIssuedForWorkOrderException extends InvoicingDomainException {

    public VoucherAlreadyIssuedForWorkOrderException(WorkOrderId workOrderId, String existingReference) {
        super(
                "ERR_VOUCHER_ALREADY_ISSUED",
                "Work order " + workOrderId.value() + " already has an active electronic voucher (" + existingReference + ")."
        );
    }
}
