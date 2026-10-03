package com.andeva.atelier.platform.billing.application.queryservices;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.queries.ListTenantInvoicesQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;

import java.util.List;
import java.util.Optional;

/**
 * Application Query Service contract for retrieving SaaS billing invoice receipts.
 *
 * @author Joel Huamani Estefanero
 */
public interface SaasInvoiceQueryService {

    /**
     * Retrieves the complete billing invoice receipt history for a workshop tenant.
     *
     * @param query ListTenantInvoicesQuery
     * @return list of SaasInvoice aggregates
     */
    List<SaasInvoice> handle(ListTenantInvoicesQuery query);

    /**
     * Finds an invoice by its internal universal identifier.
     *
     * @param id SaasInvoiceId
     * @return Optional containing SaasInvoice if found
     */
    Optional<SaasInvoice> findById(SaasInvoiceId id);

    /**
     * Finds an invoice by its Stripe external invoice identifier.
     *
     * @param stripeInvoiceId StripeInvoiceId
     * @return Optional containing SaasInvoice if found
     */
    Optional<SaasInvoice> findByStripeInvoiceId(StripeInvoiceId stripeInvoiceId);
}
