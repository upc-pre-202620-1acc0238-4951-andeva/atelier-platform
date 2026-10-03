package com.andeva.atelier.platform.shared.application.handlers;

import com.andeva.atelier.platform.shared.domain.events.DomainEvent;

/**
 * Base functional contract for in-memory consumers of domain events emitted by aggregate roots.
 *
 * @param <E> concrete domain event type
 * @author Joel Huamani Estefanero
 */
@FunctionalInterface
public interface DomainEventHandler<E extends DomainEvent> {

    /**
     * Handles the domain event dispatch.
     *
     * @param event the emitted domain event
     */
    void handle(E event);
}
