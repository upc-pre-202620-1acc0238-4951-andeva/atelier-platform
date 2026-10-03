package com.andeva.atelier.platform.shared.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable contract identifying every domain event across the Atelier Platform.
 * Provides required metadata for auditing, event streaming, and Transactional Outbox persistence.
 *
 * @author Joel Huamani Estefanero
 */
public interface DomainEvent {
    /**
     * Unique identifier of the event for deduplication and idempotency.
     *
     * @return unique event UUID
     */
    UUID eventId();

    /**
     * Exact UTC timestamp when the event occurred in the domain.
     *
     * @return occurrence instant
     */
    Instant occurredOn();

    /**
     * Identifier of the Aggregate Root that originated the state mutation.
     *
     * @return aggregate identifier
     */
    String aggregateId();

    /**
     * Canonical or qualified name of the emitted event type.
     *
     * @return event type name
     */
    String eventType();
}
