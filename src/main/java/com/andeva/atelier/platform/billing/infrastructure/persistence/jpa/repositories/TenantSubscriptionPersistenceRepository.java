package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.TenantSubscriptionPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link TenantSubscriptionPersistenceEntity}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface TenantSubscriptionPersistenceRepository extends JpaRepository<TenantSubscriptionPersistenceEntity, UUID> {

    /**
     * Finds a subscription by workshop tenant ID.
     *
     * @param tenantId workshop tenant UUID
     * @return optional containing the subscription entity
     */
    Optional<TenantSubscriptionPersistenceEntity> findByTenantId(UUID tenantId);

    /**
     * Finds a subscription by Stripe Subscription ID.
     *
     * @param stripeSubscriptionId Stripe subscription identifier (e.g. sub_123)
     * @return optional containing the subscription entity
     */
    Optional<TenantSubscriptionPersistenceEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    /**
     * Retrieves all subscriptions matching a specific lifecycle status.
     *
     * @param status subscription status enum
     * @return list of matching subscriptions
     */
    List<TenantSubscriptionPersistenceEntity> findAllByStatus(SubscriptionStatus status);

    /**
     * Checks if a tenant has an existing subscription with any of the specified statuses.
     *
     * @param tenantId workshop tenant UUID
     * @param statuses collection of statuses
     * @return true if matching subscription exists
     */
    boolean existsByTenantIdAndStatusIn(UUID tenantId, Collection<SubscriptionStatus> statuses);
}
