package com.andeva.atelier.platform.billing.interfaces.rest.transform;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SaasInvoiceResource;
import com.andeva.atelier.platform.billing.interfaces.rest.resources.responses.SaasInvoiceSummaryResource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Assembler transforming {@link SaasInvoice} domain aggregates into detailed and summary REST resources.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SaasInvoiceResourceAssembler {

    /**
     * Transforms an invoice aggregate root into its comprehensive REST representation.
     *
     * @param invoice non-null invoice domain aggregate
     * @return detailed invoice resource
     */
    public SaasInvoiceResource toResource(SaasInvoice invoice) {
        Objects.requireNonNull(invoice, "SaasInvoice cannot be null");
        return new SaasInvoiceResource(
                invoice.id().value(),
                invoice.subscriptionId().value(),
                invoice.tenantId().value(),
                invoice.stripeInvoiceId().value(),
                invoice.amountPaid().amount(),
                invoice.amountPaid().currency().name(),
                invoice.status().name(),
                invoice.invoicePdfUrl(),
                invoice.hostedInvoiceUrl(),
                invoice.paidAt().orElse(null),
                invoice.paidAt().orElse(java.time.Instant.now())
        );
    }

    /**
     * Transforms an invoice aggregate root into a compact summary resource for tabular listings.
     *
     * @param invoice non-null invoice domain aggregate
     * @return lightweight invoice summary resource
     */
    public SaasInvoiceSummaryResource toSummaryResource(SaasInvoice invoice) {
        Objects.requireNonNull(invoice, "SaasInvoice cannot be null");
        return new SaasInvoiceSummaryResource(
                invoice.id().value(),
                invoice.stripeInvoiceId().value(),
                invoice.amountPaid().amount(),
                invoice.amountPaid().currency().name(),
                invoice.status().name(),
                invoice.paidAt().orElse(null)
        );
    }

    /**
     * Transforms a collection of invoice aggregates into a list of summary resources.
     *
     * @param invoices collection of domain invoices
     * @return unmodifiable list of summary resources
     */
    public List<SaasInvoiceSummaryResource> toSummaryResourceList(List<SaasInvoice> invoices) {
        if (invoices == null || invoices.isEmpty()) {
            return Collections.emptyList();
        }
        return invoices.stream()
                .filter(Objects::nonNull)
                .map(this::toSummaryResource)
                .toList();
    }
}
