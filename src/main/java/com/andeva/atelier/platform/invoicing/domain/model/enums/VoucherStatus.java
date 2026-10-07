package com.andeva.atelier.platform.invoicing.domain.model.enums;

/**
 * Lifecycle states of an electronic fiscal voucher.
 *
 * @author Joel Huamani Estefanero
 */
public enum VoucherStatus {
    DRAFT,
    ISSUED,
    ACCEPTED_SUNAT,
    REJECTED_SUNAT,
    VOIDED
}
