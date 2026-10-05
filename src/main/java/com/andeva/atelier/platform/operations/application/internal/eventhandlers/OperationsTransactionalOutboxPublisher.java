package com.andeva.atelier.platform.operations.application.internal.eventhandlers;

import com.andeva.atelier.platform.operations.interfaces.events.*;
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

import java.time.Instant;
import java.util.Objects;

@Component
public class OperationsTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OperationsTransactionalOutboxPublisher.class);

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OperationsTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.objectMapper.findAndRegisterModules();
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(WorkOrderCreatedIntegrationEvent event) {
        persistOutbox("WorkOrder", event.workOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(WorkOrderBayAssignedIntegrationEvent event) {
        persistOutbox("WorkOrder", event.workOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(WorkOrderStartedIntegrationEvent event) {
        persistOutbox("WorkOrder", event.workOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ProductStockReservationRequestedIntegrationEvent event) {
        persistOutbox("WorkOrderTask", event.taskId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(ProductStockReservationCancelledIntegrationEvent event) {
        persistOutbox("WorkOrderTask", event.taskId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(WorkOrderCompletedIntegrationEvent event) {
        persistOutbox("WorkOrder", event.workOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(WorkOrderPaidIntegrationEvent event) {
        persistOutbox("WorkOrder", event.workOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(WorkOrderDeliveredIntegrationEvent event) {
        persistOutbox("WorkOrder", event.workOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    private void persistOutbox(String aggregateType, String aggregateId, String eventType, Object payload, Instant occurredOn) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxMessagePersistenceEntity outbox = OutboxMessagePersistenceEntity.pendingOf(
                    aggregateType,
                    aggregateId,
                    eventType,
                    payloadJson,
                    occurredOn != null ? occurredOn : Instant.now()
            );
            outboxRepository.save(outbox);
            log.debug("Persisted outbox integration event: {} for aggregate: {}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize integration event {} to JSON payload", eventType, e);
            throw new IllegalStateException("Could not serialize event to JSON: " + eventType, e);
        }
    }
}
