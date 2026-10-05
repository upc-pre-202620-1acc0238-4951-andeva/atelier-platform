package com.andeva.atelier.platform.inventory.application.internal.eventhandlers;

import com.andeva.atelier.platform.inventory.interfaces.events.InventoryItemCreatedIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.LowStockAlertIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.PurchaseOrderIssuedIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.PurchaseOrderReceivedIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.StockAllocatedIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.StockReleasedIntegrationEvent;
import com.andeva.atelier.platform.inventory.interfaces.events.SupplierRegisteredIntegrationEvent;
import com.andeva.atelier.platform.shared.infrastructure.outbox.entities.OutboxMessagePersistenceEntity;
import com.andeva.atelier.platform.shared.infrastructure.outbox.repositories.OutboxMessageJpaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.Objects;

@Component
public class InventoryTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(InventoryTransactionalOutboxPublisher.class);

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public InventoryTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "outboxRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
        this.objectMapper.findAndRegisterModules();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(InventoryItemCreatedIntegrationEvent event) {
        persistOutbox("InventoryItem", event.itemId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StockAllocatedIntegrationEvent event) {
        persistOutbox("InventoryItem", event.itemId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(StockReleasedIntegrationEvent event) {
        persistOutbox("InventoryItem", event.itemId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(LowStockAlertIntegrationEvent event) {
        persistOutbox("InventoryItem", event.itemId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PurchaseOrderIssuedIntegrationEvent event) {
        persistOutbox("PurchaseOrder", event.purchaseOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PurchaseOrderReceivedIntegrationEvent event) {
        persistOutbox("PurchaseOrder", event.purchaseOrderId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SupplierRegisteredIntegrationEvent event) {
        persistOutbox("Supplier", event.supplierId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
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
