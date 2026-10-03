package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.billing.domain.model.aggregates.TenantSubscription;
import com.andeva.atelier.platform.billing.domain.model.enums.SubscriptionStatus;
import com.andeva.atelier.platform.billing.domain.model.ids.SubscriptionId;
import com.andeva.atelier.platform.billing.domain.model.valueobjects.StripeSubscriptionId;
import com.andeva.atelier.platform.billing.domain.repositories.TenantSubscriptionRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.TenantSubscriptionPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.TenantSubscriptionPersistenceRepository;
import com.andeva.atelier.platform.shared.domain.model.valueobjects.TenantId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Spring Data JPA adapter implementation of {@link TenantSubscriptionRepository}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional(readOnly = true)
public class TenantSubscriptionRepositoryImpl implements TenantSubscriptionRepository {

    private final TenantSubscriptionPersistenceRepository springRepo;
    private final TenantSubscriptionPersistenceAssembler assembler;

    public TenantSubscriptionRepositoryImpl(
            TenantSubscriptionPersistenceRepository springRepo,
            TenantSubscriptionPersistenceAssembler assembler
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "TenantSubscriptionPersistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "TenantSubscriptionPersistenceAssembler cannot be null");
    }

    @Override
    @Transactional
    public TenantSubscription save(TenantSubscription subscription) {
        Objects.requireNonNull(subscription, "TenantSubscription cannot be null");
        return assembler.toDomain(springRepo.save(assembler.toEntity(subscription)));
    }

    @Override
    public Optional<TenantSubscription> findById(SubscriptionId id) {
        if (id == null) {
            return Optional.empty();
        }
        return springRepo.findById(id.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<TenantSubscription> findByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return Optional.empty();
        }
        return springRepo.findByTenantId(tenantId.value()).map(assembler::toDomain);
    }

    @Override
    public Optional<TenantSubscription> findByStripeSubscriptionId(StripeSubscriptionId stripeSubId) {
        if (stripeSubId == null) {
            return Optional.empty();
        }
        return springRepo.findByStripeSubscriptionId(stripeSubId.value()).map(assembler::toDomain);
    }

    @Override
    public boolean existsActiveByTenantId(TenantId tenantId) {
        if (tenantId == null) {
            return false;
        }
        return springRepo.existsByTenantIdAndStatusIn(
                tenantId.value(),
                List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING)
        );
    }

    @Override
    public List<TenantSubscription> findAllByStatus(SubscriptionStatus status) {
        if (status == null) {
            return List.of();
        }
        return springRepo.findAllByStatus(status).stream()
                .map(assembler::toDomain)
                .toList();
    }
}
