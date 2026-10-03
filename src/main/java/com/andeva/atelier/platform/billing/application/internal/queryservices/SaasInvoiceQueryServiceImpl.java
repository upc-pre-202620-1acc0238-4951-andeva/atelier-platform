package com.andeva.atelier.platform.billing.application.internal.queryservices;

import com.andeva.atelier.platform.billing.application.queryservices.SaasInvoiceQueryService;
import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.queries.ListTenantInvoicesQuery;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.repositories.SaasInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Implementation of SaasInvoiceQueryService for querying SaaS billing invoices by tenant or identifiers.
 *
 * @author Joel Huamani Estefanero
 */
@Service
@Transactional(readOnly = true)
public class SaasInvoiceQueryServiceImpl implements SaasInvoiceQueryService {

    private final SaasInvoiceRepository invoiceRepository;

    public SaasInvoiceQueryServiceImpl(SaasInvoiceRepository invoiceRepository) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository, "SaasInvoiceRepository cannot be null");
    }

    @Override
    public List<SaasInvoice> handle(ListTenantInvoicesQuery query) {
        Objects.requireNonNull(query, "ListTenantInvoicesQuery cannot be null");
        return invoiceRepository.findAllByTenantId(query.tenantId());
    }

    @Override
    public Optional<SaasInvoice> findById(SaasInvoiceId id) {
        Objects.requireNonNull(id, "SaasInvoiceId cannot be null");
        return invoiceRepository.findById(id);
    }

    @Override
    public Optional<SaasInvoice> findByStripeInvoiceId(StripeInvoiceId stripeInvoiceId) {
        Objects.requireNonNull(stripeInvoiceId, "StripeInvoiceId cannot be null");
        return invoiceRepository.findByStripeInvoiceId(stripeInvoiceId);
    }
}
