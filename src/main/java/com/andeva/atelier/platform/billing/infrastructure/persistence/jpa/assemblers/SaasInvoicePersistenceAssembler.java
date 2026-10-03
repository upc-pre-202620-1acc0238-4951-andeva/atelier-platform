package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SaasInvoicePersistenceEntity;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Currency;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.Money;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Component;

/**
 * Assembler transforming between {@link SaasInvoice} Domain Aggregate Root
 * and {@link SaasInvoicePersistenceEntity} JPA entity.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class SaasInvoicePersistenceAssembler {

    /**
     * Converts a domain aggregate into a JPA persistence entity.
     *
     * @param domain domain aggregate root
     * @return JPA persistence entity
     */
    public SaasInvoicePersistenceEntity toEntity(SaasInvoice domain) {
        if (domain == null) {
            return null;
        }

        SaasInvoicePersistenceEntity entity = new SaasInvoicePersistenceEntity(domain.id().value());
        entity.setSubscriptionId(domain.subscriptionId().value());
        entity.setTenantId(domain.tenantId().value());
        entity.setStripeInvoiceId(domain.stripeInvoiceId().value());
        entity.setAmountPaid(domain.amountPaid().amount());
        entity.setCurrency(domain.currency().name());
        entity.setStatus(domain.status());
        entity.setInvoicePdfUrl(domain.invoicePdfUrl());
        entity.setHostedInvoiceUrl(domain.hostedInvoiceUrl());
        entity.setPaidAt(domain.paidAt().orElse(null));

        return entity;
    }

    /**
     * Reconstitutes a domain aggregate from a JPA persistence entity.
     *
     * @param entity JPA persistence entity
     * @return domain aggregate root
     */
    public SaasInvoice toDomain(SaasInvoicePersistenceEntity entity) {
        if (entity == null) {
            return null;
        }

        SaasInvoiceId id = SaasInvoiceId.of(entity.getId());
        SubscriptionId subscriptionId = SubscriptionId.of(entity.getSubscriptionId());
        TenantId tenantId = new TenantId(entity.getTenantId());
        StripeInvoiceId stripeInvoiceId = new StripeInvoiceId(entity.getStripeInvoiceId());
        Currency currency = Currency.valueOf(entity.getCurrency() != null ? entity.getCurrency() : "USD");
        Money amountPaid = Money.of(entity.getAmountPaid(), currency);

        return new SaasInvoice(
                id,
                subscriptionId,
                tenantId,
                stripeInvoiceId,
                amountPaid,
                currency,
                entity.getStatus(),
                entity.getInvoicePdfUrl(),
                entity.getHostedInvoiceUrl(),
                entity.getPaidAt()
        );
    }
}
