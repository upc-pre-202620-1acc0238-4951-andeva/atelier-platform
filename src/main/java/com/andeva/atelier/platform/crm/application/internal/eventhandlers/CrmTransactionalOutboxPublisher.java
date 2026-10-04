package com.andeva.atelier.platform.crm.application.internal.eventhandlers;

import com.andeva.atelier.platform.crm.interfaces.events.AppointmentArrivedIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.AppointmentScheduledIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.CustomerCreatedIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.VehicleOwnershipTransferredIntegrationEvent;
import com.andeva.atelier.platform.crm.interfaces.events.VehicleRegisteredIntegrationEvent;
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

/**
 * Transactional Outbox Publisher for Customer and Fleet Management (CRM) integration events.
 * Atomically serializes and records integration events into {@code outbox_messages} table
 * within the active transaction to guarantee reliable delivery across bounded contexts.
 *
 * @author Adiel Sanchez Santin
 */
@Component
public class CrmTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(CrmTransactionalOutboxPublisher.class);

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public CrmTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "OutboxMessageJpaRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper cannot be null");
        this.objectMapper.findAndRegisterModules();
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(CustomerCreatedIntegrationEvent event) {
        persistOutboxMessage("Customer", event.customerId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VehicleRegisteredIntegrationEvent event) {
        persistOutboxMessage("Vehicle", event.vehicleId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(VehicleOwnershipTransferredIntegrationEvent event) {
        persistOutboxMessage("Vehicle", event.vehicleId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(AppointmentScheduledIntegrationEvent event) {
        persistOutboxMessage("Appointment", event.appointmentId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(AppointmentArrivedIntegrationEvent event) {
        persistOutboxMessage("Appointment", event.appointmentId().toString(), event.getClass().getSimpleName(), event, event.occurredOn());
    }

    private void persistOutboxMessage(
            String aggregateType,
            String aggregateId,
            String eventType,
            Object event,
            Instant occurredOn
    ) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxMessagePersistenceEntity outboxEntity = OutboxMessagePersistenceEntity.pendingOf(
                    aggregateType,
                    aggregateId,
                    eventType,
                    payload,
                    occurredOn != null ? occurredOn : Instant.now()
            );
            outboxRepository.save(outboxEntity);
            log.debug("CRM integration event persisted to Outbox: type={}, aggregateId={}", eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize CRM integration event for Outbox: {}", event, e);
            throw new IllegalStateException("Failed to serialize CRM integration event for outbox", e);
        }
    }
}
