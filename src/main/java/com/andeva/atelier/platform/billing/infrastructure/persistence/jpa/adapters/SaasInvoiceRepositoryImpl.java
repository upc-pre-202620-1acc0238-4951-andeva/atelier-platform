package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SaasInvoice;
import com.andeva.atelier.platform.billing.domain.model.ids.SaasInvoiceId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeInvoiceId;
import com.andeva.atelier.platform.billing.domain.repositories.SaasInvoiceRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.SaasInvoicePersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.SaasInvoicePersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring Data JPA adapter implementation of {@link SaasInvoiceRepository}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional(readOnly = true)
public class SaasInvoiceRepositoryImpl implements SaasInvoiceRepository {

    private final SaasInvoicePersistenceRepository springRepo;
    private final SaasInvoicePersistenceAssembler assembler;

    public SaasInvoiceRepositoryImpl(
            SaasInvoicePersistenceRepository springRepo,
            SaasInvoicePersistenceAssembler assembler
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "SaasInvoicePersistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "SaasInvoicePersistenceAssembler cannot be null");
    }

    @Override
    @Transactional
    public SaasInvoice save(SaasInvoice invoice) {
        Objects.requireNonNull(invoice, "SaasInvoice cannot be null");
        return assembler.toDomain(springRepo.save(assembler.toEntity(invoice)));
    }

    @Override
    public Optional<SaasInvoice> findById(SaasInvoiceId id) {
        if (id == null) {
            return Optional.empty();
        }
        return springRepo.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<SaasInvoice> findByStripeInvoiceId(StripeInvoiceId stripeInvoiceId) {
        if (stripeInvoiceId == null) {
            return Optional.empty();
        }
        return springRepo.findByStripeInvoiceId(stripeInvoiceId.value()).map(assembler::toDomain);
    }

    @Override
    public List<SaasInvoice> findAllByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return List.of();
        }
        return springRepo.findAllByTenantIdOrderByCreatedAtDesc(tenantId.value()).stream()
                .map(assembler::toDomain)
                .toList();
    }
}
