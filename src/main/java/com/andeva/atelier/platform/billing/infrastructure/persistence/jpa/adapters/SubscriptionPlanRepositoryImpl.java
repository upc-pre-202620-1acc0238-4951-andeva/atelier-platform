package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.billing.domain.model.aggregates.SubscriptionPlan;
import com.andeva.atelier.platform.billing.domain.model.ids.PlanId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripePriceId;
import com.andeva.atelier.platform.billing.domain.repositories.SubscriptionPlanRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.SubscriptionPlanPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.SubscriptionPlanPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring Data JPA adapter implementation of {@link SubscriptionPlanRepository}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional(readOnly = true)
public class SubscriptionPlanRepositoryImpl implements SubscriptionPlanRepository {

    private final SubscriptionPlanPersistenceRepository springRepo;
    private final SubscriptionPlanPersistenceAssembler assembler;

    public SubscriptionPlanRepositoryImpl(
            SubscriptionPlanPersistenceRepository springRepo,
            SubscriptionPlanPersistenceAssembler assembler
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "SubscriptionPlanPersistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "SubscriptionPlanPersistenceAssembler cannot be null");
    }

    @Override
    @Transactional
    public SubscriptionPlan save(SubscriptionPlan plan) {
        Objects.requireNonNull(plan, "SubscriptionPlan cannot be null");
        return assembler.toDomain(springRepo.save(assembler.toEntity(plan)));
    }

    @Override
    public Optional<SubscriptionPlan> findById(PlanId id) {
        if (id == null) {
            return Optional.empty();
        }
        return springRepo.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<SubscriptionPlan> findByStripePriceId(StripePriceId stripePriceId) {
        if (stripePriceId == null) {
            return Optional.empty();
        }
        return springRepo.findByStripePriceId(stripePriceId.value()).map(assembler::toDomain);
    }

    @Override
    public List<SubscriptionPlan> findAllActive() {
        return springRepo.findAllByIsActiveTrue().stream()
                .map(assembler::toDomain)
                .toList();
    }
}
