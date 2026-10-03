package com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.adapters;

import com.andeva.atelier.platform.billing.domain.model.aggregates.StripeWebhookEvent;
import com.andeva.atelier.platform.billing.domain.model.ids.StripeEventId;
import com.andeva.atelier.platform.billing.domain.repositories.StripeWebhookEventRepository;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.assemblers.StripeWebhookEventPersistenceAssembler;
import com.andeva.atelier.platform.billing.infrastructure.persistence.jpa.repositories.StripeWebhookEventPersistenceRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

/**
 * Spring Data JPA adapter implementation of {@link StripeWebhookEventRepository}.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
@Transactional(readOnly = true)
public class StripeWebhookEventRepositoryImpl implements StripeWebhookEventRepository {

    private final StripeWebhookEventPersistenceRepository springRepo;
    private final StripeWebhookEventPersistenceAssembler assembler;

    public StripeWebhookEventRepositoryImpl(
            StripeWebhookEventPersistenceRepository springRepo,
            StripeWebhookEventPersistenceAssembler assembler
    ) {
        this.springRepo = Objects.requireNonNull(springRepo, "StripeWebhookEventPersistenceRepository cannot be null");
        this.assembler = Objects.requireNonNull(assembler, "StripeWebhookEventPersistenceAssembler cannot be null");
    }

    @Override
    @Transactional
    public StripeWebhookEvent save(StripeWebhookEvent event) {
        Objects.requireNonNull(event, "StripeWebhookEvent cannot be null");
        return assembler.toDomain(springRepo.save(assembler.toEntity(event)));
    }

    @Override
    public Optional<StripeWebhookEvent> findByStripeEventId(StripeEventId stripeEventId) {
        if (stripeEventId == null) {
            return Optional.empty();
        }
        return springRepo.findByStripeEventId(stripeEventId.value()).map(assembler::toDomain);
    }

    @Override
    public boolean existsByStripeEventId(StripeEventId stripeEventId) {
        if (stripeEventId == null) {
            return false;
        }
        return springRepo.existsByStripeEventId(stripeEventId.value());
    }
}
