package com.andeva.atelier.platform.shared.infrastructure.outbox.publisher;

import com.andeva.atelier.platform.shared.application.events.DomainEventPublisher;
import com.andeva.atelier.platform.shared.domain.events.DomainEvent;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Objects;

/**
 * Output adapter implementing the {@link DomainEventPublisher} port from the Application Layer.
 * Atomically persists domain events into the PostgreSQL {@code outbox_messages} table
 * within the active transaction under the Transactional Outbox pattern, and complements
 * this with in-memory dispatch to Spring application listeners.
 *
 * @author Joel Huamani Estefanero
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JpaDomainEventPublisher implements DomainEventPublisher {

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(DomainEvent event) {
        Objects.requireNonNull(event, "Domain event cannot be null");

        try {
            String payload = objectMapper.writeValueAsString(event);
            String aggregateType = event.getClass().getPackageName();
            String aggregateId = event.aggregateId() != null ? event.aggregateId() : "UNKNOWN";
            String eventType = event.eventType();

            OutboxMessagePersistenceEntity outboxEntity = OutboxMessagePersistenceEntity.pendingOf(
                    aggregateType,
                    aggregateId,
                    eventType,
                    payload,
                    event.occurredOn()
            );

            outboxRepository.save(outboxEntity);
            log.debug("Outbox event persisted: type={}, id={}", eventType, event.eventId());

            // Complementary in-memory publication for local Spring listeners
            applicationEventPublisher.publishEvent(event);

        } catch (JsonProcessingException e) {
            log.error("Critical failure serializing domain event for Outbox: {}", event, e);
            throw new IllegalStateException("Failed to serialize domain event for outbox", e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void publishAll(Collection<Object> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        for (Object event : events) {
            if (event instanceof DomainEvent domainEvent) {
                publish(domainEvent);
            } else {
                log.warn("Object in events queue does not implement DomainEvent: {}", event);
                applicationEventPublisher.publishEvent(event);
            }
        }
    }
}
