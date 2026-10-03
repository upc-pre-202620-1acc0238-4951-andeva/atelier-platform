package com.andeva.atelier.platform.billing.domain.repositories;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository contract for managing recurring SaaS invoice receipts.
 *
 * @author Joel Huamani Estefanero
 */
public interface SaasInvoiceRepository {

    /**
     * Saves or updates a SaaS invoice receipt in persistence.
     *
     * @param invoice SaasInvoice aggregate to persist
     * @return persisted SaasInvoice instance
     */
    SaasInvoice save(SaasInvoice invoice);

    /**
     * Finds a SaaS invoice receipt by its internal unique identifier.
     *
     * @param id SaasInvoiceId
     * @return Optional containing the invoice if found
     */
    Optional<SaasInvoice> findById(SaasInvoiceId id);

    /**
     * Finds a SaaS invoice receipt by its external Stripe invoice identifier.
     *
     * @param stripeInvoiceId StripeInvoiceId
     * @return Optional containing the invoice if found
     */
    Optional<SaasInvoice> findByStripeInvoiceId(StripeInvoiceId stripeInvoiceId);

    /**
     * Retrieves the complete billing invoice history for a given workshop tenant.
     *
     * @param tenantId TenantId
     * @return list of SaasInvoices belonging to the tenant
     */
    List<SaasInvoice> findAllByTenantId(TenantId tenantId);
}
