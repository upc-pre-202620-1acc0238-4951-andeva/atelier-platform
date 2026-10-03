package com.andeva.atelier.platform.billing.application.internal.eventhandlers;

import com.andeva.atelier.platform.billing.interfaces.events.TenantPlanUpgradedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionStatusChangedIntegrationEvent;
import com.andeva.atelier.platform.billing.interfaces.events.TenantSubscriptionSuspendedIntegrationEvent;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Transactional Outbox Publisher for Billing Integration Events.
 * Atomically serializes and registers integration events into the PostgreSQL {@code outbox_messages} table
 * within the active transaction to guarantee At-Least-Once Delivery to platform brokers.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class BillingTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingTransactionalOutboxPublisher.class);
    private static final String AGGREGATE_TYPE = "TenantSubscription";

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public BillingTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxMessageJpaRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
        this.objectMapper.findAndRegisterModules();
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(TenantSubscriptionStatusChangedIntegrationEvent event) {
        persistOutboxMessage(event.subscriptionId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(TenantPlanUpgradedIntegrationEvent event) {
        persistOutboxMessage(event.subscriptionId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(TenantSubscriptionSuspendedIntegrationEvent event) {
        persistOutboxMessage(event.subscriptionId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    private void persistOutboxMessage(
            String aggregateId,
            String eventType,
            Object event,
            java.time.Instant occurredOn
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxMessagePersistenceEntity outboxEntity = OutboxMessagePersistenceEntity.pendingOf(
                    AGGREGATE_TYPE,
                    aggregateId,
                    eventType,
                    payload,
                    occurredOn != null ? occurredOn : java.time.Instant.now()
            );
            outboxRepository.save(outboxEntity);
            log.debug("Billing integration event persisted to Outbox: type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize billing integration event for Outbox: {}", event, e);
            throw new IllegalStateException("Failed to serialize billing integration event for outbox", e);
        }
    }
}
