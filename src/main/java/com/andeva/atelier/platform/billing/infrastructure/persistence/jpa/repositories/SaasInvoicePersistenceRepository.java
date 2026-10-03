package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SaasInvoicePersistenceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SaasInvoicePersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface SaasInvoicePersistenceRepository extends JpaRepository<SaasInvoicePersistenceEntity, UUID> {

    /**
     * Finds an invoice by its unique Stripe Invoice ID.
     *
     * @param stripeInvoiceId Stripe invoice identifier (e.g. in_123)
     * @return optional containing the matching entity
     */
    Optional<SaasInvoicePersistenceEntity> findByStripeInvoiceId(String stripeInvoiceId);

    /**
     * Retrieves all invoices for a tenant ordered chronologically descending.
     *
     * @param tenantId workshop tenant identifier
     * @return list of invoices
     */
    List<SaasInvoicePersistenceEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    /**
     * Retrieves paginated invoices for a tenant ordered chronologically descending.
     *
     * @param tenantId workshop tenant identifier
     * @param pageable pagination parameters
     * @return list of invoices
     */
    List<SaasInvoicePersistenceEntity> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);
}
