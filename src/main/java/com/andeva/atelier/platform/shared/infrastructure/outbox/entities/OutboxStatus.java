package com.andeva.atelier.platform.shared.infrastructure.outbox.entities;

/**
 * Enumeration defining the transactional lifecycle status of an Outbox message
 * registered in the persistence store for asynchronous broker dispatch.
 *
 * @author Joel Huamani Estefanero
 */
public enum OutboxStatus {
    /**
     * Message is persisted and waiting for asynchronous dispatcher worker pickup.
     */
    PENDING,

    /**
     * Message has been successfully published to the message broker.
     */
    PUBLISHED,

    /**
     * Message failed to publish after maximum retries.
     */
    FAILED
}
