package com.andeva.atelier.platform.billing.application.commandservices;

import com.andeva.atelier.platform.billing.domain.model.commands.RecordSaasInvoicePaymentCommand;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;

/**
 * Application Command Service contract for recording SaaS recurring billing invoices and receipts.
 *
 * @author Joel Huamani Estefanero
 */
public interface SaasInvoiceCommandService {

    /**
     * Records a settled invoice receipt issued by Stripe for a tenant subscription.
     *
     * @param command RecordSaasInvoicePaymentCommand
     * @return newly recorded SaasInvoiceId
     */
    SaasInvoiceId handle(RecordSaasInvoicePaymentCommand command);

    /**
     * Marks an existing SaaS invoice payment attempt as failed.
     *
     * @param invoiceId SaasInvoiceId
     * @param reason    failure explanation
     */
    void handleMarkFailed(SaasInvoiceId invoiceId, String reason);

    /**
     * Voids or invalidates a SaaS invoice receipt.
     *
     * @param invoiceId SaasInvoiceId
     */
    void handleMarkVoid(SaasInvoiceId invoiceId);
}
