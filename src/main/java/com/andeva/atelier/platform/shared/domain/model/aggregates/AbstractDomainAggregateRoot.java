package com.andeva.atelier.platform.shared.domain.model.aggregates;

import org.springframework.data.domain.AbstractAggregateRoot;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Abstract superclass for all Aggregate Roots across the Atelier Platform.
 * Extends Spring Data Commons' AbstractAggregateRoot strictly to accumulate
 * in-memory domain events without coupling the domain to JPA infrastructure.
 *
 * @param <T> Concrete aggregate root type
 * @author Joel Huamani Estefanero
 */
public abstract class AbstractDomainAggregateRoot<T extends AbstractDomainAggregateRoot<T>>
        extends AbstractAggregateRoot<T> {

    /**
     * Enqueues an immutable domain event for subsequent dispatching.
     *
     * @param event The domain event instance that occurred
     */
    protected void registerDomainEvent(Object event) {
        Objects.requireNonNull(event, "Domain event to register cannot be null");
        super.registerEvent(event);
    }

    /**
     * Returns an unmodifiable view of the accumulated in-memory domain events.
     *
     * @return unmodifiable collection of domain events
     */
    @Override
    public Collection<Object> domainEvents() {
        return Collections.unmodifiableCollection(super.domainEvents());
    }

    /**
     * Clears the internal queue of domain events once persisted or dispatched
     * to the Transactional Outbox table.
     */
    @Override
    public void clearDomainEvents() {
        super.clearDomainEvents();
    }
}
