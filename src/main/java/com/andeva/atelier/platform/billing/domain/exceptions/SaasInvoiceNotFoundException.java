package com.andeva.atelier.platform.billing.domain.exceptions;

import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;

/**
 * Thrown when a SaaS invoice record cannot be found by its identifier.
 *
 * @author Joel Huamani Estefanero
 */
public class SaasInvoiceNotFoundException extends BillingDomainException {

    public SaasInvoiceNotFoundException(SaasInvoiceId invoiceId) {
        super("SAAS_INVOICE_NOT_FOUND", "SaaS invoice not found with identifier: " + (invoiceId != null ? invoiceId.value() : "null"));
    }

    public SaasInvoiceNotFoundException(StripeInvoiceId stripeInvoiceId) {
        super("SAAS_INVOICE_NOT_FOUND", "SaaS invoice not found with Stripe invoice identifier: " + (stripeInvoiceId != null ? stripeInvoiceId.value() : "null"));
    }

    public SaasInvoiceNotFoundException(String message) {
        super("SAAS_INVOICE_NOT_FOUND", message);
    }
}
