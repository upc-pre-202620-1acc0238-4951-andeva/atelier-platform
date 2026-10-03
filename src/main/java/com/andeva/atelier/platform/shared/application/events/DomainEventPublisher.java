package com.andeva.atelier.platform.shared.application.events;

import com.andeva.atelier.platform.shared.domain.events.DomainEvent;

import java.util.Collection;

/**
 * Application layer port for dispatching domain events towards in-memory subscribers
 * or towards the Transactional Outbox persistence store for eventual delivery to message brokers.
 *
 * @author Joel Huamani Estefanero
 */
public interface DomainEventPublisher {

    /**
     * Publishes a single immutable domain event.
     *
     * @param event domain event to dispatch
     */
    void publish(DomainEvent event);

    /**
     * Publishes a collection of events extracted from an aggregate root (e.g. via aggregate.domainEvents()).
     *
     * @param events collection of in-memory accumulated events
     */
    void publishAll(Collection<Object> events);
}
