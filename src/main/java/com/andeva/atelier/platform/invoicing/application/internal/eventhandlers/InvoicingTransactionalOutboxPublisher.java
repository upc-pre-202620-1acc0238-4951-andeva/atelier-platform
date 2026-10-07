package com.andeva.atelier.platform.invoicing.application.internal.eventhandlers;

import com.andeva.atelier.platform.invoicing.interfaces.events.ElectronicVoucherIssuedIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherAcceptedBySunatIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherPaymentRegisteredIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherRejectedBySunatIntegrationEvent;
import com.andeva.atelier.platform.invoicing.interfaces.events.VoucherVoidedIntegrationEvent;
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
import java.util.UUID;

/**
 * Transactional Outbox Publisher for Invoicing & Compliance integration events.
 * Atomically records integration events in the PostgreSQL {@code outbox_messages} table
 * within the active transaction to guarantee At-Least-Once Delivery.
 *
 * @author Joel Huamani Estefanero
 */
@Component
public class InvoicingTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(InvoicingTransactionalOutboxPublisher.class);
    private static final String AGGREGATE_TYPE = "ElectronicVoucher";

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public InvoicingTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxMessageJpaRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
        this.objectMapper.findAndRegisterModules();
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(ElectronicVoucherIssuedIntegrationEvent event) {
        persistOutboxMessage(event.voucherId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherAcceptedBySunatIntegrationEvent event) {
        persistOutboxMessage(event.voucherId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherRejectedBySunatIntegrationEvent event) {
        persistOutboxMessage(event.voucherId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherVoidedIntegrationEvent event) {
        persistOutboxMessage(event.voucherId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VoucherPaymentRegisteredIntegrationEvent event) {
        persistOutboxMessage(event.paymentId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    public void publish(String aggregateId, String eventType, Object payload, Instant occurredOn) {
        persistOutboxMessage(aggregateId, eventType, payload, occurredOn);
    }

    private void persistOutboxMessage(String aggregateId, String eventType, Object payload, Instant occurredOn) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxMessagePersistenceEntity outboxEntity = OutboxMessagePersistenceEntity.pendingOf(
                    AGGREGATE_TYPE,
                    aggregateId,
                    eventType,
                    jsonPayload,
                    occurredOn != null ? occurredOn : Instant.now()
            );
            outboxRepository.save(outboxEntity);
            log.debug("Persisted integration event {} [aggregateId={}] in transactional outbox", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize integration event {} to JSON for transactional outbox: {}", eventType, e.getMessage(), e);
            throw new IllegalStateException("Outbox serialization failure for event: " + eventType, e);
        }
    }
}
