package com.andeva.atelier.platform.hr.application.internal.eventhandlers;

import com.andeva.atelier.platform.hr.domain.model.events.*;
import com.andeva.atelier.platform.hr.interfaces.events.MechanicClockedInIntegrationEvent;
import com.andeva.atelier.platform.hr.interfaces.events.MechanicClockedOutIntegrationEvent;
import com.andeva.atelier.platform.hr.interfaces.events.PayrollProcessedIntegrationEvent;
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
import java.util.UUID;

@Component
public class HrTransactionalOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(HrTransactionalOutboxPublisher.class);

    private final OutboxMessageJpaRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public HrTransactionalOutboxPublisher(
            OutboxMessageJpaRepository outboxRepository,
            ObjectMapper objectMapper
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "outboxRepository cannot be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper cannot be null");
        this.objectMapper.findAndRegisterModules();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(EmployeeClockedInEvent event) {
        MechanicClockedInIntegrationEvent integrationEvent = new MechanicClockedInIntegrationEvent(
                event.attendanceRecordId().value(),
                event.tenantId().value(),
                event.branchId().value(),
                event.membershipId().value(),
                event.clockIn(),
                event.occurredOn()
        );
        persistOutbox("AttendanceRecord", event.attendanceRecordId().value().toString(),
                integrationEvent.getClass().getSimpleName(), integrationEvent, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(EmployeeClockedOutEvent event) {
        MechanicClockedOutIntegrationEvent integrationEvent = new MechanicClockedOutIntegrationEvent(
                event.attendanceRecordId().value(),
                event.tenantId().value(),
                event.membershipId().value(),
                event.clockOut(),
                event.totalWorkedMinutes(),
                event.occurredOn()
        );
        persistOutbox("AttendanceRecord", event.attendanceRecordId().value().toString(),
                integrationEvent.getClass().getSimpleName(), integrationEvent, event.occurredOn());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(PayrollDisbursedEvent event) {
        PayrollProcessedIntegrationEvent integrationEvent = new PayrollProcessedIntegrationEvent(
                event.payrollPaymentId().value(),
                event.tenantId().value(),
                event.membershipId().value(),
                event.totalPaid().amount(),
                event.totalPaid().currency().name(),
                event.paymentReference(),
                event.occurredOn()
        );
        persistOutbox("PayrollPayment", event.payrollPaymentId().value().toString(),
                integrationEvent.getClass().getSimpleName(), integrationEvent, event.occurredOn());
    }

    private void persistOutbox(String aggregateType, String aggregateId, String eventType, Object payload, Instant occurredOn) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            OutboxMessagePersistenceEntity entity = OutboxMessagePersistenceEntity.pendingOf(
                    aggregateType,
                    aggregateId,
                    eventType,
                    jsonPayload,
                    occurredOn != null ? occurredOn : Instant.now()
            );

            outboxRepository.save(entity);
            log.debug("Persisted outbox event for HR aggregate {}: {}", aggregateType, eventType);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize HR event payload for outbox: {}", eventType, e);
        } catch (Exception e) {
            log.error("Unexpected error saving HR outbox event {}: {}", eventType, e.getMessage(), e);
        }
    }
}
