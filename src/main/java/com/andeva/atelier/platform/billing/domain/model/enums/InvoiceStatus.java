package com.andeva.atelier.platform.billing.domain.model.enums;

/**
 * Accounting and payment status for SaaS subscription invoices.
 *
 * @author Joel Huamani Estefanero
 */
public enum InvoiceStatus {
    DRAFT,
    PAID,
    OPEN,
    VOID,
    UNCOLLECTIBLE
}
