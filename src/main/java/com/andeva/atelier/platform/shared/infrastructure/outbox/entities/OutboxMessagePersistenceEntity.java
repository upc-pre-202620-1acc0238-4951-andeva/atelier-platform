package com.andeva.atelier.platform.shared.infrastructure.outbox.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA persistence entity mapped to the {@code outbox_messages} table.
 * Represents an immutable record of a serialized domain event that must be reliably
 * dispatched to external brokers via the Transactional Outbox pattern.
 *
 * @author Joel Huamani Estefanero
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "outbox_messages",
        indexes = {
                @Index(name = "idx_outbox_status_occurred_on", columnList = "status, occurred_on")
        }
)
public class OutboxMessagePersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 100)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "occurred_on", nullable = false, updatable = false)
    private Instant occurredOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "last_error", columnDefinition = "text")
    private String lastError;

    @Column(name = "processed_at")
    private Instant processedAt;

    /**
     * Creates a new pending outbox message persistence entity.
     *
     * @param aggregateType canonical type or package of the aggregate root
     * @param aggregateId   unique identifier of the aggregate
     * @param eventType     canonical event name
     * @param payload       serialized JSON payload
     * @param occurredOn    instant when the domain event occurred
     * @return initialized pending entity
     */
    public static OutboxMessagePersistenceEntity pendingOf(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload,
            Instant occurredOn
    ) {
        OutboxMessagePersistenceEntity entity = new OutboxMessagePersistenceEntity();
        entity.setAggregateType(Objects.requireNonNull(aggregateType, "Aggregate type cannot be null"));
        entity.setAggregateId(Objects.requireNonNull(aggregateId, "Aggregate ID cannot be null"));
        entity.setEventType(Objects.requireNonNull(eventType, "Event type cannot be null"));
        entity.setPayload(Objects.requireNonNull(payload, "Payload cannot be null"));
        entity.setOccurredOn(Objects.requireNonNull(occurredOn, "Occurred on instant cannot be null"));
        entity.setStatus(OutboxStatus.PENDING);
        entity.setRetryCount(0);
        return entity;
    }
}
