package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories;

import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link SubscriptionPlanPersistenceEntity}.
 * Uses {@link EntityGraph} for performant, eager fetching of plan features on demanded queries.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface SubscriptionPlanPersistenceRepository extends JpaRepository<SubscriptionPlanPersistenceEntity, UUID> {

    @Override
    @EntityGraph(attributePaths = {"features"})
    Optional<SubscriptionPlanPersistenceEntity> findById(UUID id);

    /**
     * Finds a plan by its official Stripe Price ID with features fetched.
     *
     * @param stripePriceId the Stripe price token (e.g. price_123)
     * @return optional containing the matching entity
     */
    @EntityGraph(attributePaths = {"features"})
    Optional<SubscriptionPlanPersistenceEntity> findByStripePriceId(String stripePriceId);

    /**
     * Retrieves all plans currently active for public onboarding with features fetched.
     *
     * @return list of active plans
     */
    @EntityGraph(attributePaths = {"features"})
    List<SubscriptionPlanPersistenceEntity> findAllByIsActiveTrue();
}
