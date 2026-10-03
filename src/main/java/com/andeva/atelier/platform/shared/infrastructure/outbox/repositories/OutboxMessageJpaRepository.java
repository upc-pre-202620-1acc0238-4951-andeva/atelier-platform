package com.andeva.atelier.platform.shared.infrastructure.outbox.repositories;

import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for polling and managing outbox messages in the persistence store.
 *
 * @author Joel Huamani Estefanero
 */
@Repository
public interface OutboxMessageJpaRepository extends JpaRepository<OutboxMessagePersistenceEntity, UUID> {

    /**
     * Retrieves up to 50 outbox messages with the specified status ordered chronologically by occurrence instant.
     * Used by the asynchronous background dispatcher worker to stream events towards message brokers.
     *
     * @param status processing lifecycle status (e.g. PENDING)
     * @return ordered list of outbox messages ready for dispatch
     */
    List<OutboxMessagePersistenceEntity> findTop50ByStatusOrderByOccurredOnAsc(OutboxStatus status);

    /**
     * Retrieves up to 50 outbox messages filtered by status and aggregate type ordered chronologically.
     * Prevents starvation when multiple bounded contexts share the outbox table.
     *
     * @param status processing lifecycle status (e.g. PENDING)
     * @param aggregateType canonical type or package of the aggregate root
     * @return ordered list of outbox messages ready for dispatch
     */
    List<OutboxMessagePersistenceEntity> findTop50ByStatusAndAggregateTypeOrderByOccurredOnAsc(OutboxStatus status, String aggregateType);
}
